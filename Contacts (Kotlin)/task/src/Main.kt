package contacts

import com.squareup.moshi.*
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import java.io.File
import java.time.LocalDateTime
import java.util.*
import kotlin.system.exitProcess


interface PhoneTypeEnum {

}

abstract class PhoneNumber(val isPerson: Boolean) {

    var created: String = LocalDateTime.now().toString()
    var modified: String = LocalDateTime.now().toString()

    abstract fun displayValue(memberName: String): String

    abstract fun changeProperty(memberName: String, memberValue: String)
    abstract fun changebleProperties(): List<String>
    abstract fun propertiesContains(searchValue: String): Boolean

}

// organization
enum class OrganizationEnum(val memberName: String, val textEdit: String) : PhoneTypeEnum {
    NAME("name", "Organization name"),
    ADDRESS("address", "Address"),
    NUMBER("number", "Number")
}

@JsonClass(generateAdapter = true)
class PhoneNumberOrganization(
    var name: String,
    var address: String,
    var number: String
) : PhoneNumber(isPerson = false) {

    override fun displayValue(memberName: String): String {
        val organizationEnum: OrganizationEnum = OrganizationEnum.valueOf(memberName.uppercase())

        return when (organizationEnum) {
            OrganizationEnum.NAME -> this.name
            OrganizationEnum.ADDRESS -> this.address
            OrganizationEnum.NUMBER -> this.number
        }

    }


    override fun changebleProperties(): List<String> {
        return OrganizationEnum.entries.map { it.memberName }.toList()
    }

    override fun propertiesContains(searchValue: String): Boolean {
        return OrganizationEnum.entries.map { displayValue(it.memberName) }.toList()
            .joinToString(prefix = "", postfix = "", separator = "_").lowercase()
            .contains(searchValue.lowercase().toRegex())
    }


    override fun changeProperty(memberName: String, memberValue: String) {
        val organizationEnum: OrganizationEnum = OrganizationEnum.valueOf(memberName.uppercase())

        when (organizationEnum) {
            OrganizationEnum.NAME -> this.name = memberValue
            OrganizationEnum.ADDRESS -> this.address = memberValue
            OrganizationEnum.NUMBER -> this.number = memberValue
        }
    }

}


// person

enum class PersonEnum(val memberName: String, val textEdit: String) : PhoneTypeEnum {
    NAME("name", "Name"),
    SURNAME("surname", "Surname"),
    BIRTH("birth", "Birth date"),
    GENDER("gender", "Gender"),
    NUMBER("number", "Number")
}

@JsonClass(generateAdapter = true)
class PhoneNumberPerson(
    var name: String,
    var surname: String,
    var birth: String = "[no data]",
    var gender: String = "[no data]",
    var number: String
) : PhoneNumber(isPerson = true) {

    override fun displayValue(memberName: String): String {
        val personEnum: PersonEnum = PersonEnum.valueOf(memberName.uppercase())

        return when (personEnum) {
            PersonEnum.NAME -> this.name
            PersonEnum.SURNAME -> this.surname
            PersonEnum.BIRTH -> this.birth
            PersonEnum.GENDER -> this.gender
            PersonEnum.NUMBER -> this.number
            else -> ""
        }

    }

    override fun changebleProperties(): List<String> {
        return PersonEnum.entries.map { it.memberName }.toList()
    }

    override fun propertiesContains(searchValue: String): Boolean {
        return PersonEnum.entries.map { displayValue(it.memberName) }.toList()
            .joinToString(prefix = "", postfix = "", separator = "_").lowercase()
            .contains(searchValue.lowercase().toRegex())
    }


    override fun changeProperty(memberName: String, memberValue: String) {
        val personEnum: PersonEnum = PersonEnum.valueOf(memberName.uppercase())

        when (personEnum) {
            PersonEnum.NAME -> this.name = memberValue
            PersonEnum.SURNAME -> this.surname = memberValue
            PersonEnum.BIRTH -> this.birth = memberValue
            PersonEnum.GENDER -> this.gender = memberValue
            PersonEnum.NUMBER -> this.number = memberValue
        }
    }
}


//

fun main() {
    val phoneNumbers = readContactsFromFile()

    menu(phoneNumbers)
}

private fun menu(phoneNumbers: MutableList<PhoneNumber?>) {
    do {
        println()
        print("[menu] Enter action (add, list, search, count, exit): ")

        val action = readln()
        // val action = readln()
        when (action) {
            "add" -> addPhoneNumber(phoneNumbers)
            "list" -> infoPhoneNumbers(phoneNumbers)
            "search" -> searchPhoneNumber(phoneNumbers)
            "count" -> countPhoneNumber(phoneNumbers)
            "exit" -> exitTask(phoneNumbers)
            else -> println("Invalid action")
        }
        println()
    } while (true)
}


fun searchPhoneNumber(phoneNumbers: MutableList<PhoneNumber?>) {
    if (phoneNumbers.isNotEmpty()) {
        backPhoneNumber@ while (phoneNumbers.isNotEmpty()) {

            print("Enter search query: ")
            val searchQuery = readln()
            val results = phoneNumbers.filter { it?.propertiesContains(searchQuery) ?: false }.toList()

            println("Found ${results.count()} results: ")

            var cpt = 0

            for (result in results) {
                cpt++
                if (result != null) {
                    println("${cpt}. ${result.displayValue("name")}")
                }
            }

            println()
            while (true) {
                print("[search] Enter action ([number], back, again): ")
                val action = readln()
                when {
                    isNumeric(action) -> {
                        val phoneNumber = results[action.toInt() - 1]
                        val position = phoneNumbers.indexOf(phoneNumber)
                        recordPhoneNumber(phoneNumbers, position)
                        //recordPhoneNumber(phoneNumbers, action.toInt() - 1)
                    }

                    action == "back" -> break@backPhoneNumber
                    action == "again" -> break
                }
            }
        }
    }

}

private fun recordPhoneNumber(phoneNumbers: MutableList<PhoneNumber?>, position: Int) {
    while (true) {
        print("[record] Enter action (edit, delete, menu): ")
        val action = readln()
        when (action) {
            "edit" -> editPhoneNumber(phoneNumbers, position)
            "delete" -> removePhoneNumber(phoneNumbers, position)
            "menu" -> menu(phoneNumbers)
        }
    }
}


fun editPhoneNumber(phoneNumbers: MutableList<PhoneNumber?>, recordNumber: Int) {

    val phoneNumber = phoneNumbers[recordNumber]

    if (phoneNumber != null) {
        print("Select a field (${phoneNumber.changebleProperties().joinToString(separator = ",")}): ")
    }
    val fieldName = readln()
    if (phoneNumber != null) {
        phoneNumbers[recordNumber] = editValue(phoneNumber, fieldName)
        if (phoneNumber.isPerson) {
            infoPhoneNumberPerson(phoneNumber as PhoneNumberPerson)
        } else {
            infoPhoneNumberOrganization(phoneNumber as PhoneNumberOrganization)
        }
        println()
    }
}


fun editValue(phoneNumber: PhoneNumber, memberName: String): PhoneNumber {
    print("Enter the $memberName: ")
    val scanner = Scanner(System.`in`)
    val memberValue = scanner.nextLine()
    phoneNumber.changeProperty(memberName, memberValue)
    phoneNumber.modified = LocalDateTime.now().toString()
    println("saved")
    return phoneNumber
}


fun addPhoneNumber(phoneNumbers: MutableList<PhoneNumber?>) {
    print("Enter the type (person, organization): ")
    val typeSelected = readln()

    when (typeSelected) {
        "person" -> addPhoneNumberPerson(phoneNumbers)
        "organization" -> addPhoneNumberOrganization(phoneNumbers)
        else -> println("Invalid type")
    }
}


fun addPhoneNumberOrganization(phoneNumbers: MutableList<PhoneNumber?>) {
    val scanner = Scanner(System.`in`)

    print("Enter the organization name: ")
    val name = scanner.nextLine()

    print("Enter the address: ")
    val address = scanner.nextLine()

    print("Enter the number: ")
    val number = scanner.nextLine()

    if (phoneNumberValidity(number)) {
        phoneNumbers.add(PhoneNumberOrganization(name, address, number))
        println("The record added.")
        println()
    } else {
        println("Wrong number format!")
        phoneNumbers.add(PhoneNumberOrganization(name, address, "[no number]"))
    }
}

fun addPhoneNumberPerson(phoneNumbers: MutableList<PhoneNumber?>) {
    val scanner = Scanner(System.`in`)

    print("Enter the ${PersonEnum.NAME.textEdit}: ")
    val name: String = scanner.nextLine()

    print("Enter the ${PersonEnum.SURNAME.textEdit}: ")
    val surname: String = scanner.nextLine()

    print("Enter the ${PersonEnum.BIRTH.textEdit}: ")
    val birthDateToSave: String = setBirthDate(scanner)

    print("Enter the ${PersonEnum.GENDER.textEdit}: ")
    val genderToSave: String = setGender(scanner)

    print("Enter the ${PersonEnum.NUMBER.textEdit}: ")
    val numberToSave: String = setNumber(scanner)

    phoneNumbers.add(PhoneNumberPerson(name, surname, birthDateToSave, genderToSave, numberToSave))
    println("The record added.")
    println()

}

private fun setNumber(scanner: Scanner): String {
    val number: String = scanner.nextLine()
    if (phoneNumberValidity(number)) {
        return number
    } else {
        return "[no number]"
    }
}

private fun setGender(scanner: Scanner): String {
    val genderToSave: String
    val gender = scanner.nextLine()
    if (gender.isNotBlank()) {
        genderToSave = gender
    } else {
        genderToSave = "[no data]"
        println("Bad gender!")
    }
    return genderToSave
}

private fun setBirthDate(scanner: Scanner): String {
    val birth = scanner.nextLine()
    val birthDateToSave: String
    if (birth.isNotBlank()) {
        birthDateToSave = birth
    } else {
        birthDateToSave = "[no data]"
        println("Bad birth date!")
    }
    return birthDateToSave
}


fun removePhoneNumber(phoneNumbers: MutableList<PhoneNumber?>, recordNumber: Int) {
    if (phoneNumbers.isEmpty()) {
        println("No records to remove!")
    } else if (phoneNumbers.size < recordNumber) {
        println("No records to remove!")
        println()
    } else {
        if (recordNumber <= phoneNumbers.count()) {
            phoneNumbers.removeAt(recordNumber)
            println("The record removed!")
            println()
        }
    }
}


fun countPhoneNumber(phoneNumbers: MutableList<PhoneNumber?>) {
    val nb = phoneNumbers.count()
    println("The Phone Book has $nb records.")
}

fun infoPhoneNumbers(phoneNumbers: MutableList<PhoneNumber?>) {
    if (phoneNumbers.isNotEmpty()) {
        while (true) {
            showPhoneBookList(phoneNumbers)
            println()
            print("[list] Enter action ([number], back): ")
            val indexStr = readln()

            when {
                isNumeric(indexStr) -> {
                    val index = indexStr.toInt() - 1
                    infoPhoneNumber(phoneNumbers, index)
                }

                indexStr == "back" -> {
                    println(); menu(phoneNumbers)
                }
            }

        }
    } else {
        println("No records to list!")
    }
}

private fun infoPhoneNumber(phoneNumbers: MutableList<PhoneNumber?>, index: Int) {
    if (phoneNumbers.isNotEmpty()) {
        val phoneNumber = phoneNumbers[index]

        if (phoneNumber != null) {
            if (phoneNumber.isPerson) {
                infoPhoneNumberPerson(phoneNumber as PhoneNumberPerson)
                println()
                recordPhoneNumber(phoneNumbers, index)
                return
            } else {
                infoPhoneNumberOrganization(phoneNumber as PhoneNumberOrganization)
                println()
                recordPhoneNumber(phoneNumbers, index)
                return
            }
        }
    }

}


private fun showPhoneBookList(phoneNumbers: MutableList<PhoneNumber?>) {
    var position = 0
    for (phoneNumber in phoneNumbers) {
        if (phoneNumber != null) {
            if (phoneNumber.isPerson) {
                phoneNumber as PhoneNumberPerson
                position++
                println("$position. ${phoneNumber.name} ${phoneNumber.surname}")
            } else {
                phoneNumber as PhoneNumberOrganization
                position++
                println("$position. ${phoneNumber.name}")
            }
        }
    }
}

fun infoPhoneNumberPerson(phoneNumber: PhoneNumberPerson) {
    println("Name: ${phoneNumber.name}")
    println("Surname: ${phoneNumber.surname}")
    println("Birth date: ${phoneNumber.birth}")
    println("Gender: ${phoneNumber.gender}")
    println("Number: ${phoneNumber.number}")
    phoneNumber.javaClass.declaredFields

    println("Time created: ${phoneNumber.created}")
    println("Time last edit: ${phoneNumber.modified}")
}

fun infoPhoneNumberOrganization(phoneNumber: PhoneNumberOrganization) {
    println("Organization name: ${phoneNumber.name}")
    println("Address: ${phoneNumber.address}")
    println("Number: ${phoneNumber.number}")
    println("Time created: ${phoneNumber.created}")
    println("Time last edit: ${phoneNumber.modified}")
}

fun exitTask(phoneNumbers: MutableList<PhoneNumber?>) {
    if (phoneNumbers.isNotEmpty()) {
        savePhoneNumber(phoneNumbers)
    }

    exitProcess(0)
}

fun phoneNumberValidity(value: String): Boolean {
    val regex =
        """(([+]{1}([\d][\s])?)?((\w{2,5}))+(([\s-]{1})([(]?(\w{2,5})[)]?))*)|(([+]{1}([\d][\s])?)?([(]?(\w{2,5})[)]?)+(([\s-]{1})((\w{2,5})))*)""".toRegex()
    return value.matches(regex)
}

fun isNumeric(toCheck: String): Boolean {
    val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
    return toCheck.matches(regex)
}


fun readContactsFromFile(): MutableList<PhoneNumber?> {
    val jsonFile = File("phonebook.db")

    if (jsonFile.exists()) {

        val phoneNumberListAdapter = getJsonPhoneNumberAdapter()

        if (phoneNumberListAdapter != null) {
            val phoneNumbers = phoneNumberListAdapter.fromJson(jsonFile.readText())

            if (phoneNumbers != null) {
                return phoneNumbers.toMutableList()
            }
        }
    }
    return mutableListOf<PhoneNumber?>()
}

fun savePhoneNumber(phoneNumbers: MutableList<PhoneNumber?>) {

    val phoneNumberListAdapter = getJsonPhoneNumberAdapter()
    val jsonFile = File("phonebook.db")

    if (phoneNumbers.isNotEmpty() && phoneNumberListAdapter != null) {
        jsonFile.writeText(phoneNumberListAdapter.toJson(phoneNumbers))
    }
}

private fun getJsonPhoneNumberAdapter(): JsonAdapter<List<PhoneNumber?>>? {
    val adapterFactory = PolymorphicJsonAdapterFactory
        .of(PhoneNumber::class.java, "isPerson")
        .withSubtype(PhoneNumberOrganization::class.java, false.toString())
        .withSubtype(PhoneNumberPerson::class.java, true.toString())

    val moshi = Moshi.Builder()
        .add(adapterFactory)
        .add(KotlinJsonAdapterFactory())
        .build()

    val type = Types.newParameterizedType(List::class.java, PhoneNumber::class.java)
    val phoneNumberListAdapter = moshi.adapter<List<PhoneNumber?>>(type)
    return phoneNumberListAdapter
}