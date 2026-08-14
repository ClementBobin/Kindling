package dev.kindling.core.components.ui.maskInput

import androidx.compose.ui.text.input.KeyboardType

private val EmailRegex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
private val UriRegex = Regex("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$")

enum class KMaskPattern(
    val pattern: String,
    val placeholder: String = "",
    val keyboardType: KeyboardType = KeyboardType.Number,
    val withoutMask: Boolean = false,
    val validate: ((String) -> Boolean)? = null
) {
    Phone("(###) ###-####", "(555) 000-0000", validate = { getUnmaskedValue(it).length == 10 }),
    Ssn("###-##-####", "000-00-0000", validate = { getUnmaskedValue(it).length == 9 }),
    Date("##/##/####", "MM/DD/YYYY", validate = { getUnmaskedValue(it).length == 8 }),
    Time("##:##", "HH:MM", validate = { getUnmaskedValue(it).length == 4 }),
    CreditCard("#### #### #### ####", "0000 0000 0000 0000", validate = { getUnmaskedValue(it).length == 16 }),
    CreditCardExpiry("##/##", "MM/YY", validate = { getUnmaskedValue(it).length == 4 }),
    ZipCode("#####", "00000", validate = { getUnmaskedValue(it).length == 5 }),
    ZipCodeExtended("#####-####", "00000-0000", validate = { getUnmaskedValue(it).length == 9 }),
    Ein("##-#######", "00-0000000", validate = { getUnmaskedValue(it).length == 9 }),
    Isbn("###-#-###-#####-#", "000-0-000-00000-0", validate = { getUnmaskedValue(it).length == 13 }),
    LicensePlate(
        "###-###", 
        "AAA-000", 
        KeyboardType.Text, 
        validate = { getUnmaskedValue(it, allowLetters = true).length == 6 }
    ),
    MacAddress(
        "##:##:##:##:##:##", 
        "00:00:00:00:00:00", 
        KeyboardType.Text, 
        validate = { getUnmaskedValue(it, allowLetters = true).length == 12 }
    ),
    Email(
        "", 
        "name@example.com", 
        KeyboardType.Email, 
        withoutMask = true, 
        validate = { EmailRegex.matches(it) }
    ),
    Uri(
        "", 
        "https://", 
        KeyboardType.Uri, 
        withoutMask = true, 
        validate = { UriRegex.matches(it) }
    ),
    Number(
        "", 
        "", 
        KeyboardType.Number, 
        withoutMask = true, 
        validate = { it.toDoubleOrNull() != null }
    ),
    Decimal(
        "", 
        "0.00", 
        KeyboardType.Decimal, 
        withoutMask = true, 
        validate = { it.toDoubleOrNull() != null }
    ),
}