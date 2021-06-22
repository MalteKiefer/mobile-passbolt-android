package com.passbolt.mobile.android.core.networking

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
sealed class NetworkResult<T : Any> {

    class Success<T : Any>(
        val value: T
    ) : NetworkResult<T>()

    sealed class Failure<T : Any>(
        val exception: Exception,
        val errorCode: Int? = null,
        val headerMessage: String
    ) : NetworkResult<T>() {

        class ServerError<T : Any>(
            exception: Exception,
            errorCode: Int? = null,
            headerMessage: String
        ) : Failure<T>(exception, errorCode, headerMessage)

        class NetworkError<T : Any>(
            exception: Exception,
            headerMessage: String
        ) : Failure<T>(exception, null, headerMessage)
    }
}
