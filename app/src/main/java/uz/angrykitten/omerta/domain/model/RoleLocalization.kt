package uz.angrykitten.omerta.domain.model

/**
 * Returns the role name in [lang], falling back to whichever non-blank field is
 * available (EN → RU → UZ). Custom roles authored in a single language still
 * render — instead of showing an empty title.
 */
fun Role.localizedName(lang: AppLanguage): String = when (lang) {
    AppLanguage.EN -> nameEn
    AppLanguage.RU -> nameRu
    AppLanguage.UZ -> nameUz
}.ifBlank { firstNonBlank(nameEn, nameRu, nameUz) }

fun Role.localizedDescription(lang: AppLanguage): String = when (lang) {
    AppLanguage.EN -> descriptionEn
    AppLanguage.RU -> descriptionRu
    AppLanguage.UZ -> descriptionUz
}.ifBlank { firstNonBlank(descriptionEn, descriptionRu, descriptionUz) }

private fun firstNonBlank(vararg values: String): String =
    values.firstOrNull { it.isNotBlank() }.orEmpty()
