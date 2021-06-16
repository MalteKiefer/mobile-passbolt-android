package com.passbolt.mobile.android.feature.setup.fingerprint

import com.passbolt.mobile.android.core.mvp.CoroutineLaunchContext
import com.passbolt.mobile.android.storage.cache.passphrase.PotentialPassphrase
import com.passbolt.mobile.android.storage.repository.passphrase.PassphraseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

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
class FingerprintPresenter(
    private val fingerprintProvider: FingerprintInformationProvider,
    private val passphraseRepository: PassphraseRepository,
    coroutineLaunchContext: CoroutineLaunchContext
) : FingerprintContract.Presenter {

    override var view: FingerprintContract.View? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + coroutineLaunchContext.ui)

    override fun resume() {
        if (fingerprintProvider.hasBiometricSetUp()) {
            view?.showUseFingerprint()
        } else {
            view?.showConfigureFingerprint()
        }
    }

    override fun useFingerprintButtonClick() {
        if (fingerprintProvider.hasBiometricSetUp()) {
            view?.showBiometricPrompt()
        } else {
            view?.navigateToBiometricSettings()
        }
    }

    override fun authenticationSucceeded() {
        when (val cachedPassphrase = passphraseRepository.getPotentialPassphrase()) {
            is PotentialPassphrase.Passphrase -> {
                // fingerprint is good and passphrase in cache not expired - renew cache duration
                passphraseRepository.setPassphrase(cachedPassphrase.passphrase)
                view?.showEncourageAutofillDialog()
            }
            is PotentialPassphrase.PassphraseNotPresent -> {
                // user stayed too long and passphrase cache expired - show info dialog
                view?.showPasswordCacheExpiredDialog()
            }
        }
    }

    override fun cacheExpiredDialogConfirmed() {
        view?.navigateToEnterPassphrase()
    }

    override fun authenticationError(errorMessage: Int) {
        view?.showAuthenticationError(errorMessage)
    }

    override fun setupAutofillLaterClick() {
        view?.navigateToLogin()
    }
}
