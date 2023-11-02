package com.example.flagged


import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

data class Flag(val name : String, var stock : Int, var price : Int, val description : String, val image : String, val category : String)
data class User(val username : String, var password : String, val email : String, var favouriteFlags : ArrayList<String>)

class FirestoreDB {
    private val flags: MutableList<Flag> = mutableListOf()
    private val users: MutableList<User> = mutableListOf()
    private val db  = FirebaseFirestore.getInstance()

    init {
        getFlags()
        getUsers()
    }
    fun getFlags() : MutableList<Flag> {
        flags.clear()
        runBlocking {
            try {
                val data = db.collection("flags")
                    .get()
                    .await()
                for (document in data!!) {
                    if (document.id == "placeholder")
                        continue
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
        if (flags.contains(flag)) {
            return false
        }
        var success : Boolean
        runBlocking {
            success = try {
                db.collection("flags")
                    .document(flag.name)
                    .set(flag)
                    .await()
                println("Flag added")
                flags.add(flag)
                true
            } catch (e: Exception) {
                println("Error adding flag: $e")
                false
            }
        }
        return success
    }

    fun updateStock(name: String, amount: Int) : Error {
        val flag = flags.find { it.name == name }
        var error : Error
        if (flag == null) {
            return Error("Flag not found")
        }

        if ((flag.stock + amount) < 0) {

            return Error("Not enough stock")
        }

        runBlocking {
            error = try {
                flag.stock += amount
                db.collection("flags")
                    .document(name)
                    .set(flag)
                    .await()
                Error("")
            } catch (e: Exception) {
                println("Error updating stock: $e")
                Error(e.toString())
            }
        }

        return error
    }

    fun updatePrice(name : String, price : Int) : Boolean{
        if(price < 0)
            return false
        if(flags.find { it.name == name } == null)
            return false
        var success : Boolean
        runBlocking {
            success = try {
                db.collection("flags")
                    .document(name)
                    .update("price", price)
                    .await()
                println("Price updated")
                flags.find { it.name == name }?.price = price
                true
            } catch (e: Exception) {
                println("Error updating price: $e")
                false
            }
        }
        return success
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
        users.clear()
        runBlocking {
            try {
                val data = db.collection("users")
                    .get()
                    .await()

                for (document in data!!) {
                    if (document.id == "placeholder")
                        continue
                    val user = User(
                        username = document.id,
                        password = document.data["password"]!!.toString(),
                        email = document.data["email"]!!.toString(),
                        favouriteFlags = (document.data["favouriteFlags"] as List<String>).toCollection(ArrayList())
                    )

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
        user.password = hashPassword(user.password)
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

    fun patchUser(user: User) : Boolean {
        var success : Boolean
        success = try{
            db.collection("users")
                .document(user.username)
                .update("favouriteFlags", user.favouriteFlags)
            println("User patched")
            true
        } catch (e: Exception) {
            println("Error patching user: $e")
            false
        }
        return success
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
            println(user.password + "   :   " + hashPassword(password))
            if (user.username == username && user.password == hashPassword(password)) {
                return true
            }
        }
        return false
    }

    private fun hashPassword(password: String) : String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytesToHex(hash)
    }

    private fun bytesToHex(hash: ByteArray) : String {
        return hash.joinToString("") {"%02x".format(it) }
    }




}