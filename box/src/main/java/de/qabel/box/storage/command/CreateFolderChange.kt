package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.DirectoryMetadataFactory
import de.qabel.box.storage.FolderNavigationFactory
import de.qabel.core.crypto.CryptoUtils
import org.spongycastle.crypto.params.KeyParameter

class CreateFolderChange(
    private val name: String,
    val navigationFactory: FolderNavigationFactory,
    val directoryFactory: DirectoryMetadataFactory
) : DirectoryMetadataChange<ChangeResult<BoxFolder>> {
    private val secretKey: KeyParameter by lazy { CryptoUtils().generateSymmetricKey() }
    private val result : ChangeResult<BoxFolder> by lazy { createAndUploadDM() }

    @Synchronized
    override fun execute(dm: DirectoryMetadata): ChangeResult<BoxFolder> {
        for (folder in dm.listFolders()) {
            if (folder.name == name) {
                return ChangeResult(folder).apply { isSkipped = true }
            }
        }

        return result.apply { dm.insertFolder(boxObject) }
    }

    private fun createAndUploadDM(): ChangeResult<BoxFolder> {
        val childDM = directoryFactory.create()
        val folder = BoxFolder(childDM.fileName, name, secretKey.key)
        childDM.commit()

        with(navigationFactory.fromDirectoryMetadata(childDM, folder)) {
            setAutocommit(false)
            commit()
        }

        return ChangeResult(childDM, folder)
    }
}