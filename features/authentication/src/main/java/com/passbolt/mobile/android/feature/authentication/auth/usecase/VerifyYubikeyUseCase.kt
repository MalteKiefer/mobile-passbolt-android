package com.passbolt.mobile.android.feature.authentication.auth.usecase

import com.passbolt.mobile.android.common.CookieExtractor
import com.passbolt.mobile.android.common.usecase.AsyncUseCase
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState
import com.passbolt.mobile.android.core.networking.MfaTypeProvider
import com.passbolt.mobile.android.core.networking.NetworkResult
import com.passbolt.mobile.android.dto.request.HotpRequest
import com.passbolt.mobile.android.passboltapi.mfa.MfaRepository
import java.net.HttpURLConnection

/**
 * Passbolt - Open source password manager for teams
 * Copyright (c) 2021 Passbolt SA
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License (AGPL) as published by the Free Software Foundation version 3.
 *
 * The name "Passbolt" is a registered trademark of Passbolt SA, and Passbolt SA hereby declines to grant a trademark
 * license to "Passbolt" pursuant to the GNU Affero General Public License version 3 Section 7(e), without a separate
 * agreement with Passbolt SA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see GNU Affero General Public License v3 (http://www.gnu.org/licenses/agpl-3.0.html).
 *
 * @copyright Copyright (c) Passbolt SA (https://www.passbolt.com)
 * @license https://opensource.org/licenses/AGPL-3.0 AGPL License
 * @link https://www.passbolt.com Passbolt (tm)
 * @since v1.0
 */
class VerifyYubikeyUseCase(
    private val mfaRepository: MfaRepository,
    private val cookieExtractor: CookieExtractor
) : AsyncUseCase<VerifyYubikeyUseCase.Input, VerifyYubikeyUseCase.Output> {

    override suspend fun execute(input: Input): Output =
        when (val result = mfaRepository.verifyYubikeyOtp(
            HotpRequest(input.totp, input.remember), input.jwtHeader?.let { "Bearer $it" }
        )
        ) {
            is NetworkResult.Failure -> Output.Failure(result)
            is NetworkResult.Success -> {
                if (result.value.isSuccessful) {
                    val mfaHeader = cookieExtractor.get(result.value, CookieExtractor.MFA_COOKIE)
                    Output.Success(mfaHeader)
                } else {
                    if (result.value.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        Output.Unauthorized
                    } else {
                        Output.NetworkFailure(result.value.code())
                    }
                }
            }
        }

    class Input(
        val totp: String,
        val jwtHeader: String?,
        val remember: Boolean
    )

    sealed class Output : AuthenticatedUseCaseOutput {

        override val authenticationState: AuthenticationState
            get() = when {
                this is Failure<*> && this.response.isUnauthorized ||
                        this is NetworkFailure && this.errorCode == HttpURLConnection.HTTP_UNAUTHORIZED ->
                    AuthenticationState.Unauthenticated(AuthenticationState.Unauthenticated.Reason.Session)
                this is Failure<*> && this.response.isMfaRequired -> {
                    val providers = MfaTypeProvider.get(this.response)

                    AuthenticationState.Unauthenticated(
                        AuthenticationState.Unauthenticated.Reason.Mfa(providers)
                    )
                }
                else -> AuthenticationState.Authenticated
            }

        class Success(
            val mfaHeader: String?
        ) : Output()

        class NetworkFailure(
            val errorCode: Int
        ) : Output()

        object Unauthorized : Output()

        class Failure<T : Any>(val response: NetworkResult.Failure<T>) : Output()
    }
}
