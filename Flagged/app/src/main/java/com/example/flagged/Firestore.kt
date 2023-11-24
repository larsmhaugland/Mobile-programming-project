package com.example.flagged


import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest


val HASHITERATIONS = 2   //Iterate hash 2 times
val pepper = "pepper".toByteArray(Charsets.UTF_8) //Pepper is a salt for hashing that is not stored in the database and is the same for all users

/**
 * Data classes for Firestore
 */
data class Flag(val name : String, var stock : Int, var price : Int, val description : String, val image : String, val category : String)
data class User(val username : String = "",
                var password : String = "",
                val email : String = "",
                var favouriteFlags : ArrayList<String> = ArrayList(),
                var cart : ArrayList<ShoppingCartItem> = ArrayList())
data class ShoppingCartItem(val name: String = "", var amount: Int = 0, var price: Int = 0)

/**
 * Singleton class for Firestore database
 *
 * @property flags List of flags
 * @property users List of users
 * @property db Firestore database
 */
class FirestoreDB private constructor(){
    private val flags: MutableList<Flag> = mutableListOf()
    private val users: MutableList<User> = mutableListOf()
    private val db  = FirebaseFirestore.getInstance()

    /**
     * Companion object for singleton
     * @property instance The instance of the database
     */
    companion object {
        private val instance = FirestoreDB()

        /**
         * Initialize the database
         */
        init {
            instance.getFlags()
            instance.getUsers()
        }

        /**
         * Get the instance of the database
         * @return The instance of the database
         */
        fun getInstance() : FirestoreDB {
            return instance
        }
    }

    /**
     * Get the flags from the database
     * @return The list of flags
     */
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
                    //Add flag to local list
                    flags.add(flag)
                }
            } catch(e: Exception) {
                Log.e("Firestore", "Error getting flags: $e")
            }
        }
        return flags
    }

    /**
     * Add a flag to the database
     * @param flag The flag to add
     * @return Whether the flag was added successfully
     */
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
                Log.e("Firestore","Error adding flag: $e")
                false
            }
        }
        return success
    }

    /**
     * Update the stock of a flag
     * @param name The name of the flag
     * @param amount The amount to update the stock by
     * @return Whether the stock was updated successfully
     */
    fun updateStock(name: String, amount: Int) : Boolean {
        //Find flag in local list
        val flag = flags.find { it.name == name } ?: return false

        //Check if stock would be negative
        if ((flag.stock + amount) < 0) {
            return false
        }

        return runBlocking {
            return@runBlocking try {
                //Update stock in database
                flag.stock += amount
                db.collection("flags")
                    .document(name)
                    .set(flag)
                    .await()
                true
            } catch (e: Exception) {
                Log.e("Firestore","Error updating stock: $e")
                false
            }
        }
    }

    /**
     * Update the price of a flag
     * @param flag The flag to update
     * @return Whether the price was updated successfully
     */
    fun patchFlag(flag : Flag) : Boolean {
        return try{
            //Update flag in database
            db.collection("flags")
                .document(flag.name)
                .update("stock", flag.stock, "price", flag.price, "description", flag.description, "category", flag.category)
            true
        } catch (e: Exception) {
            Log.e("Firestore","Error patching flag: $e")
            false
        }
    }

    /**
     * Delete a flag from the database
     * @param flag The flag to delete
     * @return Whether the flag was deleted successfully
     */
    fun deleteFlag(flag : Flag) : Boolean{

        return runBlocking {
            return@runBlocking try {
                //Delete flag from database
                db.collection("flags")
                    .document(flag.name)
                    .delete()
                    .await()
                //Delete flag from local list
                flags.remove(flag)
                true
            } catch (e: Exception) {
                Log.e("Firestore","Error deleting flag: $e")
                false
            }
        }
    }

    /**
     * Get the users from the database
     * @return The list of users
     */
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
                Log.e("Firestore","Error getting users: $e")
            }
        }
        return users
    }

    /**
     * Add a user to the database
     * @param user The user to add
     * @return Whether the user was added successfully
     */
    fun addUser(user: User) : Boolean {
        //Check if user already is present in local list
        if (users.find { it.username == user.username } != null) {
            return false
        }
        //Hash password
        user.password = hashPassword(user.password, user)
        return runBlocking {
            return@runBlocking try{
                //Update collection in database
                db.collection("users")
                    .document(user.username)
                    .set(user)
                    .await()
                //Add user to local list
                users.add(user)
                true
            } catch (e: Exception) {
                Log.e("Firestore","Error adding user: $e")
                false
            }
        }
    }

    /**
     * Add a flag to a user's favourites
     * @param username The username of the user
     * @param flag The flag to add
     * @return Whether the flag was added successfully
     */
    fun addToCart(username: String, flag: String): Boolean {
        //Find user and flag in local lists
        val user = users.find { it.username == username } ?: return false
        val flagItem = flags.find { it.name == flag } ?: return false

        //Check if flag is already in shopping cart
        val cartItem = user.cart.find { it.name == flag }
        //If not, add it
        if (cartItem == null) {
            user.cart.add(ShoppingCartItem(flag, 1, flagItem.price))
        } else {
            //If it is, increase amount
            cartItem.amount++
        }
        //Update user in database
        return patchUser(user)
    }

    /**
     * Remove a flag from a user's favourites
     * @param username The username of the user
     * @param flag The flag to remove
     * @return Whether the flag was removed successfully
     */
    fun removeFromCart(username: String, flag: String) : Boolean {
        //Find user and flag in local lists
        val user = users.find { it.username == username } ?: return false
        if (user.cart.find { it.name == flag } == null) {
            return false
        }
        //Decrease amount of flag in shopping cart
        user.cart.find { it.name == flag }?.let {
            //If amount is greater than 1, decrease amount
            if (it.amount > 1) {
                it.amount--
                return patchUser(user)
            }
            //If amount is 1, remove flag from shopping cart
            user.cart.remove(it)
            //Update user in database
            return patchUser(user)
        }
        return false
    }

    /**
     * Add a flag to a user's favourites
     * @param user The user to patch
     * @return Whether the flag was added successfully
     */
    fun patchUser(user: User) : Boolean {
        return try{
            runBlocking {
                //Update user in database
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
                //Update user in local list
                users.find { it.username == user.username }?.let {
                    it.favouriteFlags = user.favouriteFlags
                    it.cart = user.cart
                }
            }
            true
        } catch (e: Exception) {
            Log.e("Firestore","Error patching user: $e")
            false
        }
    }

    /**
     * Delete a user from the database
     * @param user The user to delete
     * @return Whether the user was deleted successfully
     */
    fun deleteUser(user: User) : Boolean{
        return runBlocking {
            return@runBlocking try {
                //Delete user from database
                db.collection("users")
                    .document(user.username)
                    .delete()
                    .await()
                //Delete user from local list
                users.remove(user)
                true
            } catch (e: Exception) {
                Log.e("Firestore","Error deleting user: $e")
                false
            }
        }
    }

    /**
     * Authenticate a user
     * @param username The username of the user
     * @param password The password of the user
     * @return Whether the user was authenticated successfully
     */
    fun authUser(username: String, password: String) : Boolean {
        //Find user in local list
        for (user in users) {
            //If username matches, get hash of password and compare
            if (user.username == username && user.password == hashPassword(password, user)) {
                return true
            }
        }
        return false
    }

    /**
     * Change the password of a user
     * @param username The username of the user
     * @param oldPassword The old password of the user
     * @param newPassword The new password of the user
     * @return Whether the password was changed successfully
     */
    fun changePassword(username: String, oldPassword: String, newPassword: String) : Boolean {
        //Find user in local list
        val user = users.find { it.username == username } ?: return false
        //Check if old password matches
        if (user.password != hashPassword(oldPassword, user)) {
            return false
        }
        //Update password
        user.password = hashPassword(newPassword, user)
        return patchUser(user)
    }

    /**
     * Hash a password
     * @param password The password to hash
     * @param user The user to hash the password for
     * @return The hashed password
     */
    private fun hashPassword(password: String, user : User) : String {
        //Get SHA-512 digest
        val digest = MessageDigest.getInstance("SHA-512")
        //create salt from username
        val salt = user.username.toByteArray(Charsets.UTF_8)
        //Hash password with salt and pepper
        var hash : ByteArray = digest.digest(salt + password.toByteArray(Charsets.UTF_8) + pepper)
        //Iterate hash
        for (i in 1..HASHITERATIONS) {
            hash = digest.digest(hash)
            //Reset digest
            digest.reset()
        }
        return bytesToHex(hash)
    }
    /**
     * Convert a byte array to a hex string
     * @param hash The byte array to convert
     * @return The hex string
     */
    private fun bytesToHex(hash: ByteArray) : String {
        return hash.joinToString("") {"%02x".format(it) }
    }
}