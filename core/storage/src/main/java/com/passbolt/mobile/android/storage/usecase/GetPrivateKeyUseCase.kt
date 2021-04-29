package com.passbolt.mobile.android.storage.usecase

import com.passbolt.mobile.android.common.UseCase
import com.passbolt.mobile.android.common.extension.toCharArray
import com.passbolt.mobile.android.storage.factory.EncryptedFileFactory
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

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

class GetPrivateKeyUseCase @Inject constructor(
    private val encryptedFileFactory: EncryptedFileFactory
) : UseCase<GetPrivateKeyUseCase.Input, GetPrivateKeyUseCase.Output> {

    override fun execute(input: Input): Output {
        return try {
            val name = "${PRIVATE_KEY_FILE_NAME}_${input.userId}"
            Timber.d("Getting private key. Filename: $name")

            val encryptedFile = encryptedFileFactory.get(name, name)
            encryptedFile.openFileInput().use {
                val bytes = it.readBytes()
                Output(bytes.toCharArray())
            }
        } catch (exception: IOException) {
            Timber.e(exception)
            Output(null)
        }
    }

    class Input(
        val userId: String
    )

    class Output(
        val privateKey: CharArray?
    )
}
