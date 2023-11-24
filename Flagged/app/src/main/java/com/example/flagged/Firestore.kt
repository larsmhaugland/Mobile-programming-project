package com.example.flagged


import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import java.security.MessageDigest


val HASHITERATIONS = 2   //Iterate hash 2 times
val pepper = "pepper".toByteArray(Charsets.UTF_8) //Pepper is a salt for hashing that is not stored in the database and is the same for all users
data class Flag(val name : String, var stock : Int, var price : Int, val description : String, val image : String, val category : String)
data class User(val username : String = "",
                var password : String = "",
                val email : String = "",
                var favouriteFlags : ArrayList<String> = ArrayList(),
                var cart : ArrayList<ShoppingCartItem> = ArrayList())
data class ShoppingCartItem(val name: String = "", var amount: Int = 0, var price: Int = 0)
class FirestoreDB private constructor(){
    private val flags: MutableList<Flag> = mutableListOf()
    private val users: MutableList<User> = mutableListOf()
    private val db  = FirebaseFirestore.getInstance()
    companion object {
        private val instance = FirestoreDB()

        init {
            instance.getFlags()
            instance.getUsers()
        }

        fun getInstance() : FirestoreDB {
            return instance
        }
    }


    fun getFlags() : MutableList<Flag> {
        flags.clear() //Clear local list
        runBlocking {
            try {
                //Fetch collection from database
                val data = db.collection("flags")
                    .get()
                    .await()    //Wait for response
                for (document in data!!) {
                    //Convert each document to a flag object and add to local list
                    val flag = Flag(
                        name = document.id,
                        stock = document.data["stock"]!!.toString().toInt(),
                        price = document.data["price"]!!.toString().toInt(),
                        description = document.data["description"]!!.toString(),
                        image = document.data["image"]!!.toString(),
                        category = document.data["category"]!!.toString()
                    )
                    flags.add(flag)
                }
            } catch(e: Exception) {
                Log.e("Firestore", "Error getting flags: $e")
            }
        }
        return flags
    }
    fun addFlag(flag: Flag) : Boolean{
        //Check if flag already is present in local list
        if (flags.contains(flag)) {
            return false
        }
        var success : Boolean
        runBlocking {
            success = try {
                //Update collection in database
                db.collection("flags")
                    .document(flag.name)
                    .set(flag)
                    .await()
                //Add flag to local list
                flags.add(flag)
                true
            } catch (e: Exception) {
                println("Error adding flag: $e")
                false
            }
        }
        return success
    }

    fun updateStock(name: String, amount: Int) : Boolean {
        val flag = flags.find { it.name == name } ?: return false

        if ((flag.stock + amount) < 0) {
            return false
        }

        return runBlocking {
            return@runBlocking try {
                flag.stock += amount
                db.collection("flags")
                    .document(name)
                    .set(flag)
                    .await()
                true
            } catch (e: Exception) {
                println("Error updating stock: $e")
                false
            }
        }
    }

    fun patchFlag(flag : Flag) : Boolean {
        return try{
            db.collection("flags")
                .document(flag.name)
                .update("stock", flag.stock, "price", flag.price, "description", flag.description, "category", flag.category)
            true
        } catch (e: Exception) {
            println("Error patching flag: $e")
            false
        }
    }

    fun deleteFlag(flag : Flag) : Boolean{
        var success : Boolean
        runBlocking {
            success = try {
                db.collection("flags")
                    .document(flag.name)
                    .delete()
                    .await()
                println("Flag deleted")
                flags.remove(flag)
                true
            } catch (e: Exception) {
                println("Error deleting flag: $e")
                false
            }
        }
        return success
    }

    fun getUsers() : MutableList<User> {
        //Clear local list
        users.clear()
        runBlocking {
            try {
                //Fetch collection from database
                val data = db.collection("users")
                    .get()
                    .await()    //Wait for response

                for (document in data!!) {
                    //Convert each document to a user object and add to local list
                    val user: User = document.toObject(User::class.java)
                    users.add(user)
                }
            } catch(e: Exception) {
                println("Error getting users: $e")
            }
        }
        return users
    }

    fun addUser(user: User) : Result<Unit>{
        var result : Result<Unit>
        if (users.contains(user)) {
            return Result.failure(Error("User already exists"))
        }
        user.password = hashPassword(user.password, user)
        runBlocking {
            result = try {
                db.collection("users")
                    .document(user.username)
                    .set(user)
                    .await()

                println("User added")
                users.add(user)
                Result.success(Unit)
            } catch (e: Exception) {
                println("Error adding user: $e")
                Result.failure(e)
            }
        }
        return result
    }

    fun addToCart(username: String, flag: String): Boolean {
        val user = users.find { it.username == username } ?: return false
        val flagItem = flags.find { it.name == flag } ?: return false

        val cartItem = user.cart.find { it.name == flag }
        if (cartItem == null) {
            user.cart.add(ShoppingCartItem(flag, 1, flagItem.price))
        } else {
            cartItem.amount++
        }

        return patchUser(user)
    }


    fun removeFromCart(username: String, flag: String) : Boolean {
        val user = users.find { it.username == username } ?: return false
        if (user.cart.find { it.name == flag } == null) {
            return false
        }
        user.cart.find { it.name == flag }?.let {
            if (it.amount > 1) {
                it.amount--
                return patchUser(user)
            }
            user.cart.remove(it)
            return patchUser(user)
        }
        return false
    }

    fun patchUser(user: User) : Boolean {
        return try{
            runBlocking {
                db.collection("users")
                    .document(user.username)
                    .update(
                        "password",
                        user.password,
                        "favouriteFlags",
                        user.favouriteFlags,
                        "cart",
                        user.cart
                    )
                    .await()
                users.find { it.username == user.username }?.let {
                    it.favouriteFlags = user.favouriteFlags
                    it.cart = user.cart
                }
            }
            true
        } catch (e: Exception) {
            println("Error patching user: $e")
            false
        }
    }
    fun deleteUser(user: User) : Boolean{
        var success : Boolean
        runBlocking {
            success = try {
                db.collection("users")
                    .document(user.username)
                    .delete()
                    .await()
                println("User deleted")
                users.remove(user)
                true
            } catch (e: Exception) {
                println("Error deleting user: $e")
                false
            }
        }
        return success
    }

    fun authUser(username: String, password: String) : Boolean {
        for (user in users) {
            //If username matches, get hash of password and compare
            if (user.username == username && user.password == hashPassword(password, user)) {
                return true
            }
        }
        return false
    }

    fun changePassword(username: String, oldPassword: String, newPassword: String) : Boolean {
        val user = users.find { it.username == username } ?: return false
        if (user.password != hashPassword(oldPassword, user)) {
            return false
        }
        user.password = hashPassword(newPassword, user)
        return patchUser(user)
    }

    private fun hashPassword(password: String, user : User) : String {
        val digest = MessageDigest.getInstance("SHA-512")
        //create salt from username
        val salt = user.username.toByteArray(Charsets.UTF_8)
        //Hash password with salt and pepper
        var hash : ByteArray = digest.digest(salt + password.toByteArray(Charsets.UTF_8) + pepper)
        //Iterate hash
        for (i in 1..HASHITERATIONS) {
            hash = digest.digest(hash)
            digest.reset()
        }
        return bytesToHex(hash)
    }

    private fun bytesToHex(hash: ByteArray) : String {
        return hash.joinToString("") {"%02x".format(it) }
    }
}