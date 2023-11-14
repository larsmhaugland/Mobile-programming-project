package com.example.flagged

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.flagged", appContext.packageName)
    }

    private val db = FirestoreDB()
    @Test
    fun userTest() {
        val user = User(
            username = "test",
            password = "test",
            email = "test@test.com",
            favouriteFlags = arrayListOf()
        )
        val result = db.addUser(user)
        assertEquals(true, result.isSuccess)
        assertEquals(true,db.authUser("test","test"))

        val users = db.getUsers()
        assertEquals(true,users.contains(user))

        db.deleteUser(user)
        assertEquals(false, users.contains(user))
    }

    @Test
    fun flagTest() {
        val flag = Flag(
            name = "test",
            description = "test",
            category = "test",
            image = "test",
            price = 10,
            stock = 10
        )

        assertEquals(true,db.addFlag(flag))
        val flags = db.getFlags()
        println(flags)
        assertEquals(true, flags.contains(flag))

        db.updateStock("test", 5)
        assertEquals(15, flags.find { it.name == "test" }?.stock)
        assertEquals(true, db.updatePrice("test", 15))
        assertEquals(15, flags.find { it.name == "test" }?.price)

        db.deleteFlag(flag)
        assertEquals(false, flags.contains(flag))
    }
}