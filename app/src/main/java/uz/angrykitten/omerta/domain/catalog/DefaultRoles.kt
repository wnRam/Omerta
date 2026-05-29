package uz.angrykitten.omerta.domain.catalog

import uz.angrykitten.omerta.domain.model.Role
import uz.angrykitten.omerta.domain.model.Team

/**
 * Built-in role catalog. Names + descriptions are baked in directly (not via
 * strings.xml) because:
 *  - Roles travel over WebSocket: keeping copy on the Role object means
 *    clients can render even if their `strings.xml` is stale.
 *  - Custom roles authored by the host must work the same way — having
 *    built-ins follow the same shape removes a special case.
 *
 * Each role's [Role.iconRes] key maps via `RoleIconRegistry`.
 */
object DefaultRoles {

    val Citizen = Role(
        id = "builtin.citizen",
        nameEn = "Citizen",
        nameRu = "Мирный житель",
        nameUz = "Tinch fuqaro",
        descriptionEn = "An ordinary townsperson. Has no night ability — wins by voting out every Mafia member during the day.",
        descriptionRu = "Обычный житель города. Не имеет ночной способности — побеждает, если днём изгонят всех мафиози.",
        descriptionUz = "Oddiy shahar aholisi. Tunda qobiliyatsiz — kunduzi barcha mafiyalarni ovoz berib chiqarib yuborsa g'olib.",
        team = Team.TOWN,
        iconRes = "citizen",
    )

    val Mafia = Role(
        id = "builtin.mafia",
        nameEn = "Mafia",
        nameRu = "Мафия",
        nameUz = "Mafiya",
        descriptionEn = "Each night, conspires with other Mafia to eliminate one player. Wins when Mafia equals or outnumbers the Town.",
        descriptionRu = "Каждую ночь сговаривается с другими мафиози и убирает одного игрока. Побеждает, когда мафии становится не меньше, чем мирных.",
        descriptionUz = "Har tun boshqa mafiya bilan til biriktirib, bitta o'yinchini chiqarib tashlaydi. Mafiya soni tinch fuqarolarga tenglashganda g'alaba.",
        team = Team.MAFIA,
        iconRes = "mafia",
    )

    val Sheriff = Role(
        id = "builtin.sheriff",
        nameEn = "Sheriff",
        nameRu = "Шериф",
        nameUz = "Sherif",
        descriptionEn = "Each night, may investigate one player. Learns whether the target is aligned with the Mafia.",
        descriptionRu = "Каждую ночь может проверить одного игрока. Узнаёт, относится ли он к мафии.",
        descriptionUz = "Har tun bitta o'yinchini tekshirishi mumkin. Uning mafiyaga aloqasi borligini biladi.",
        team = Team.TOWN,
        iconRes = "sheriff",
    )

    val Doctor = Role(
        id = "builtin.doctor",
        nameEn = "Doctor",
        nameRu = "Доктор",
        nameUz = "Shifokor",
        descriptionEn = "Each night, may save one player from Mafia elimination. May not save the same player two nights in a row.",
        descriptionRu = "Каждую ночь может спасти одного игрока от мафии. Нельзя спасать одного и того же дважды подряд.",
        descriptionUz = "Har tun bitta o'yinchini mafiyadan saqlashi mumkin. Ketma-ket ikki marta bir kishini saqlash mumkin emas.",
        team = Team.TOWN,
        iconRes = "doctor",
    )

    val Don = Role(
        id = "builtin.don",
        nameEn = "Don",
        nameRu = "Дон",
        nameUz = "Don",
        descriptionEn = "Leader of the Mafia. Each night, may investigate one player to learn if they are the Sheriff.",
        descriptionRu = "Лидер мафии. Каждую ночь может проверить одного игрока — не шериф ли он.",
        descriptionUz = "Mafiya boshlig'i. Har tun bitta o'yinchini sherif yoki yo'qligini tekshira oladi.",
        team = Team.MAFIA,
        iconRes = "don",
    )

    val Maniac = Role(
        id = "builtin.maniac",
        nameEn = "Maniac",
        nameRu = "Маньяк",
        nameUz = "Manyak",
        descriptionEn = "Acts alone. Each night, may eliminate one player. Wins by being the last player standing.",
        descriptionRu = "Действует в одиночку. Каждую ночь может убрать одного игрока. Побеждает, оставшись последним.",
        descriptionUz = "Yolg'iz harakat qiladi. Har tun bitta o'yinchini chiqarib tashlashi mumkin. Oxirgi bo'lib qolsa g'olib.",
        team = Team.NEUTRAL,
        iconRes = "maniac",
    )

    val Detective = Role(
        id = "builtin.detective",
        nameEn = "Detective",
        nameRu = "Детектив",
        nameUz = "Detektiv",
        descriptionEn = "Each night, may compare any two players and learn whether they are on the same team.",
        descriptionRu = "Каждую ночь может сравнить двух игроков и узнать, в одной ли они команде.",
        descriptionUz = "Har tun ikki o'yinchini taqqoslab, bir jamoadami yoki yo'qmi bilib oladi.",
        team = Team.TOWN,
        iconRes = "detective",
    )

    val Bodyguard = Role(
        id = "builtin.bodyguard",
        nameEn = "Bodyguard",
        nameRu = "Телохранитель",
        nameUz = "Tan soqchi",
        descriptionEn = "Each night, may protect one player. If the protected player is attacked, the Bodyguard is eliminated instead.",
        descriptionRu = "Каждую ночь может защитить одного игрока. Если на подзащитного нападают, телохранитель погибает вместо него.",
        descriptionUz = "Har tun bitta o'yinchini himoya qilishi mumkin. Himoyalanganga hujum bo'lsa, tan soqchi o'rniga ketadi.",
        team = Team.TOWN,
        iconRes = "bodyguard",
    )

    val Prostitute = Role(
        id = "builtin.prostitute",
        nameEn = "Courtesan",
        nameRu = "Путана",
        nameUz = "Tungi malika",
        descriptionEn = "Each night, may visit one player and block their ability for the night. Cannot visit the same player twice in a row.",
        descriptionRu = "Каждую ночь может посетить одного игрока и заблокировать его способность на эту ночь. Нельзя посещать одного и того же дважды подряд.",
        descriptionUz = "Har tun bitta o'yinchining oldiga borib, uning kechki qobiliyatini to'sib qo'yishi mumkin. Ketma-ket bir kishiga ikki marta bormaydi.",
        team = Team.TOWN,
        iconRes = "prostitute",
    )

    val Mayor = Role(
        id = "builtin.mayor",
        nameEn = "Mayor",
        nameRu = "Мэр",
        nameUz = "Hokim",
        descriptionEn = "May reveal themselves during the day. Once revealed, their vote counts as three.",
        descriptionRu = "Может раскрыть свою роль днём. После раскрытия его голос считается за три.",
        descriptionUz = "Kunduzi o'zini ochishi mumkin. Ochilgandan keyin uning ovozi uchta sifatida hisoblanadi.",
        team = Team.TOWN,
        iconRes = "mayor",
    )

    /** Roles grouped for the Rule Setup screen. */
    val classic: List<Role> = listOf(Citizen, Mafia, Sheriff, Doctor, Don)
    val extended: List<Role> = listOf(Maniac, Detective, Bodyguard, Prostitute, Mayor)
    val all: List<Role> = classic + extended

    fun byId(id: String): Role? = all.firstOrNull { it.id == id }
}
