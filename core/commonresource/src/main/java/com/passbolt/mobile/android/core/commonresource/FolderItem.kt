package com.passbolt.mobile.android.core.commonresource

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.passbolt.mobile.android.common.extension.asBinding
import com.passbolt.mobile.android.commonresource.R
import com.passbolt.mobile.android.commonresource.databinding.ItemFolderBinding
import com.passbolt.mobile.android.ui.FolderModel
import com.passbolt.mobile.android.ui.FolderModelWithChildrenCount

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
class FolderItem(
    val folderModelWithChildrenCount: FolderModelWithChildrenCount
) : AbstractBindingItem<ItemFolderBinding>() {

    override val type: Int
        get() = R.id.itemFolder

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemFolderBinding =
        ItemFolderBinding.inflate(inflater, parent, false)

    override fun bindView(binding: ItemFolderBinding, payloads: List<Any>) {
        super.bindView(binding, payloads)
        with(binding) {
            name.text = folderModelWithChildrenCount.folderModel.name
            folderModelWithChildrenCount.folderChildrenCount.let {
                folderChildrenCount.text = it.toString()
            }
            icon.setImageResource(
                if (folderModelWithChildrenCount.folderModel.isShared) {
                    R.drawable.ic_filled_shared_folder_with_bg
                } else {
                    R.drawable.ic_filled_folder_with_bg
                }
            )
        }
    }

    class ItemClick(
        private val clickListener: (FolderModel) -> Unit
    ) : ClickEventHook<FolderItem>() {

        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.asBinding<ItemFolderBinding> {
                it.itemFolder
            }
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<FolderItem>,
            item: FolderItem
        ) {
            clickListener.invoke(item.folderModelWithChildrenCount.folderModel)
        }
    }
}
