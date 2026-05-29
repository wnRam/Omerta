package uz.angrykitten.omerta

import org.junit.Assert.assertEquals
import org.junit.Test
import uz.angrykitten.omerta.domain.model.AppLanguage
import uz.angrykitten.omerta.domain.model.Role
import uz.angrykitten.omerta.domain.model.Team
import uz.angrykitten.omerta.domain.model.localizedDescription
import uz.angrykitten.omerta.domain.model.localizedName

class RoleLocalizationTest {

    @Test
    fun `localizedName returns the requested language when present`() {
        val role = role("Citizen", "Горожанин", "Fuqaro")
        assertEquals("Citizen", role.localizedName(AppLanguage.EN))
        assertEquals("Горожанин", role.localizedName(AppLanguage.RU))
        assertEquals("Fuqaro", role.localizedName(AppLanguage.UZ))
    }

    @Test
    fun `localizedName falls back to the first non-blank language when requested is empty`() {
        val customAuthoredOnlyInUzbek = role("", "", "Maxsus rol")
        assertEquals("Maxsus rol", customAuthoredOnlyInUzbek.localizedName(AppLanguage.EN))
        assertEquals("Maxsus rol", customAuthoredOnlyInUzbek.localizedName(AppLanguage.RU))
    }

    @Test
    fun `localizedDescription respects per-language blanks independently of name`() {
        val role = Role(
            id = "r",
            nameEn = "Citizen",
            nameRu = "Горожанин",
            nameUz = "Fuqaro",
            descriptionEn = "",
            descriptionRu = "Описание",
            descriptionUz = "",
            team = Team.TOWN,
            iconRes = "citizen",
        )
        assertEquals("Описание", role.localizedDescription(AppLanguage.EN))
    }

    private fun role(en: String, ru: String, uz: String): Role = Role(
        id = "r",
        nameEn = en,
        nameRu = ru,
        nameUz = uz,
        descriptionEn = "d-en",
        descriptionRu = "d-ru",
        descriptionUz = "d-uz",
        team = Team.TOWN,
        iconRes = "citizen",
    )
}
