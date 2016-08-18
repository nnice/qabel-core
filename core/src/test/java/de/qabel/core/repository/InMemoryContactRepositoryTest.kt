package de.qabel.core.repository

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

class InMemoryContactRepositoryTest {

    val repo = InMemoryContactRepository()

    @Test
    fun testSaveFind() {
        val identityKey = QblECKeyPair()
        val identityName = "Identity"

        val identity = createIdentity(identityName, identityKey)
        val contact = createContact("Someone")

        repo.save(contact, identity)

        val sameIdentity = Identity(identityName, emptyList(), identityKey)

        val result = repo.find(sameIdentity)
        assertThat(result.contacts, hasSize(1))
    }

    @Test
    fun deleteOneExisting() {
        val identity = createIdentity("Identity", QblECKeyPair())

        val contact = createContact("Someone")
        val contact2 = createContact("Nobody")

        repo.save(contact, identity)
        repo.save(contact2, identity)
        assertEquals(2, repo.contacts.size)

        repo.delete(contact2, identity)
        assertEquals(1, repo.contacts.size)
    }

    @Test
    fun deleteOneWhichNotExists() {
        val identity = createIdentity("Identity", QblECKeyPair())
        val contact = createContact("Someone")
        val notExistingContact = createContact("Nobody")

        repo.save(contact, identity)

        try {
            repo.delete(notExistingContact, identity)
        } catch (ignored: EntityNotFoundException) {
            assertEquals(1, repo.contacts.size)
        }
    }

    @Test(expected = EntityExistsException::class)
    fun saveDuplicateContactThrowsException() {
        val identity = createIdentity("Identity", QblECKeyPair())
        val contact = createContact("Someone")
        repo.save(contact, identity)

        val contactDuplicate = createContact("Someone")
        repo.save(contactDuplicate, identity)
    }

    private fun createContact(alias: String): Contact {
        return Contact(alias, emptyList(), QblECPublicKey(alias.toByteArray()))
    }

    private fun createIdentity(identityName: String, identityKey: QblECKeyPair): Identity {
        return Identity(identityName, emptyList(), identityKey)
    }
}
