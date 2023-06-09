package svcs

import java.io.File
import java.math.BigInteger
import java.io.FileWriter


const val HELP_TEXT = """
These are SVCS commands:
config     Get and set a username.
add        Add a file to the index.
log        Show commit logs.
commit     Save changes.
checkout   Restore a file.
"""

const val CONFIG_TEXT =   "Get and set a username."
const val ADD_TEXT =      "Add a file to the index."
const val LOG_TEXT =      "Show commit logs."
const val COMMIT_TEXT =   "Save changes."
const val CHECKOUT_TEXT = "Restore a file."

fun main(args: Array<String>) {

    prepareFiles()

    if (args.size < 1 || args[0] == "--help")
    {
        print(HELP_TEXT)
    }
    else if (args[0] == "add")
    {
        add(if (args.size >= 2 ) args[1] else "")
    }
    else if (args[0] == "config")
    {
        val arg = if (args.size >= 2) args[1] else ""
        config(arg)
    }
    else if (args[0] == "commit")
    {
        val arg = if (args.size >= 2) args[1] else ""
        commit(arg)
    }
    else if (args[0] == "checkout")
    {
        val arg = if (args.size >= 2) args[1] else ""
        checkout(arg)
    }
    else if (args[0] == "log")
    {
        log()
    }
    else
    {
        print("'${args[0]}' is not a SVCS command.")
    }
}


fun prepareFiles() {
    val vcs = File("vcs")
    val config =  vcs.resolve("config.txt")
    val index =  vcs.resolve("index.txt")
    val commits = vcs.resolve("commits")
    val log = vcs.resolve("log.txt")

    if (!vcs.exists())
        vcs.mkdir()

    if (!commits.exists())
        commits.mkdir()

    if (!config.exists())
        config.createNewFile()

    if (!index.exists())
        index.createNewFile()

    if (!log.exists())
        log.createNewFile()
}

fun checkout(arg: String = "") {
    if ("" == arg) {
        println("Commit id was not passed.")
    } else {
        val path = "vcs/commits/$arg"
        if (File(path).exists()) {

            val trackedFiles = File("vcs/index.txt").readLines()

            for (file in trackedFiles) {
                //("copy $file to $dst")
                File("vcs/commits/$arg/$file").copyTo(File(file), true)

            }
            println("Switched to commit $arg.")
        } else {
            println("Commit does not exist.")
        }
    }
}


fun config(arg: String = "") {
    val file = File("vcs/config.txt")

    if ("" == arg) {

        if (file.length() == 0L) {
         println("Please, tell me who you are.")
        }
        else {
            println("The username is ${file.readText()}.")
        }
    }
    else {
        println("The username is $arg.")
        file.writeText(arg)
    }
}


fun add(arg: String = "") {
    val file = File ("vcs/index.txt")

    if (arg == "") {
        if (file.length() == 0L)
            println("Add a file to the index.")
        else {
            println("Tracked files:")
            println(file.readText())
        }
    }
    else {
        val adding_file = File("./$arg")

        if (!adding_file.exists())
            println("Can't find '$arg'.")
        else {
            file.appendText("$arg\n")
            println("The file '$arg' is tracked.")
        }
    }
}

fun log() {
    val logFile = File("vcs/log.txt")
    if(!logFile.exists() || logFile.length() == 0L) {
        print("No commits yet.")
    }
    else
    {
        val logLines = logFile.readLines()
        var i = logLines.size - 3
        while(i >= 0) {
            if (i != logLines.size - 3)
                println("\n")
            println(logLines[i])
            println(logLines[i+1])
            print(logLines[i+2])
            i -= 3
            }
    }
}

fun commit(arg: String = "") {

    if (arg == "") {
        print("Message was not passed.")
        return
    }

    val hashObj = HashUtils()
    val filesToCommit = File ("vcs/index.txt").readLines()
    var currentHash = BigInteger.valueOf(0)

    for (file in filesToCommit) {
        val hash =  hashObj.hashOfFile(file)
        currentHash += hash
        /*print("'$file' :  '$hash'")*/
    }
    val hexHash = currentHash.toString(16)
    val commit = File("vcs/commits/$hexHash")

    /* Create dir for commit and log it, if commit not exists */
    if (commit.exists()) {
        println("Nothing to commit.")
    }
    else {
        commit.mkdir()

        val userName = File("vcs/config.txt").readLines().firstOrNull()

        val commitText = "commit $hexHash\n" +
                         "Author: $userName\n" +
                         "$arg\n"

        FileWriter("vcs/log.txt", true).use {
            it.write(commitText)
        }

        /* copying all tracked files. */
        val trackedFiles = File("vcs/index.txt").readLines()

        for (file in trackedFiles) {
            val dst = commit.path + "/" + file
            //("copy $file to $dst")
            File(file).copyTo(File(dst))
        }

        println("Changes are committed.")


    }
}