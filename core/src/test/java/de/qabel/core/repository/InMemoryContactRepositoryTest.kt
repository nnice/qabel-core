package de.qabel.core.repository

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertThat
import org.junit.Test

class InMemoryContactRepositoryTest {

    val repo = InMemoryContactRepository()

    @Test
    fun testSaveFind() {
        val identityKey = QblECKeyPair()
        val identityName = "Identity"

        val identity = createIdentity(identityName, identityKey)
        val contact = createContact()

        repo.save(contact, identity)

        val sameIdentity = Identity(identityName, emptyList(), identityKey)

        val result = repo.find(sameIdentity)
        assertThat(result.contacts, hasSize(1))
    }

    @Test(expected = EntityExistsException::class)
    fun saveDuplicateContactThrowsException() {
        val identityKey = QblECKeyPair()
        val identityName = "Identity"

        val identity = createIdentity(identityName, identityKey)
        val contact = createContact()

        repo.save(contact, identity)

        val contactDuplicate = createContact()
        repo.save(contactDuplicate, identity)
    }

    private fun createContact(): Contact {
        return Contact("Test", emptyList(), QblECPublicKey("test".toByteArray()))
    }

    private fun createIdentity(identityName: String, identityKey: QblECKeyPair): Identity {
        return Identity(identityName, emptyList(), identityKey)
    }
}
