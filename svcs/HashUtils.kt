package svcs

import java.math.BigInteger
import java.security.MessageDigest
import java.io.File

class HashUtils {

    fun md5Hash(str: String) : BigInteger {
        val md = MessageDigest.getInstance("MD5")
        val bigInt = BigInteger(1, md.digest(str.toByteArray(Charsets.UTF_8)))
        return bigInt
        //return String.format("%032x", bigInt)
    }

    fun hashOfFile(path: String) : BigInteger {
        val text = File(path).readText(Charsets.UTF_8)
        return md5Hash(text)
    }
}