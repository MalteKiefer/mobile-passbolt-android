package com.passbolt.mobile.android.core.mvp.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import timber.log.Timber

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
class AuthenticatedOperationRunner(
    private val needAuthenticationRefreshedFlow: MutableStateFlow<UnauthenticatedReason?>,
    private val authenticationRefreshedFlow: StateFlow<Unit?>
) {

    suspend fun <OUTPUT : AuthenticatedUseCaseOutput> runOperation(
        request: suspend () -> OUTPUT
    ): OUTPUT {
        val response = request.invoke()
        val authenticationState = response.authenticationState
        return if (authenticationState is AuthenticationState.Unauthenticated) {
            Timber.d("Authenticated operation runner $this waits for auth refresh")
            needAuthenticationRefreshedFlow.tryEmit(authenticationState.reason)
            authenticationRefreshedFlow
                .drop(1) // drop initial value
                .take(1) // wait for first session refreshed item
                .collect {
                    Timber.d("Authenticated operation runner $this got refreshed auth")
                }
            Timber.d("Authenticated operation runner $this restarts initial operation")
            request.invoke()
        } else {
            response
        }
    }
}

suspend fun <OUTPUT : AuthenticatedUseCaseOutput> runAuthenticatedOperation(
    needAuthenticationRefresh: MutableStateFlow<UnauthenticatedReason?>,
    authenticationRefreshedFlow: StateFlow<Unit?>,
    request: suspend () -> OUTPUT
): OUTPUT =
    AuthenticatedOperationRunner(needAuthenticationRefresh, authenticationRefreshedFlow).runOperation(
        request
    )
