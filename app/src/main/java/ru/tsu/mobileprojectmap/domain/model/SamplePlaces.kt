package ru.tsu.mobileprojectmap.domain.model

object SamplePlaces {

    val places = listOf(
        Place(
            id = "starbooks",
            name = "Starbooks",
            type = PlaceType.CAFE,
            point = Point(102, 114),
            description = "Кофе, десерты и напитки рядом с учебными корпусами.",
            menuItems = listOf("coffee", "snack", "tea", "dessert"),
            openHour = 9,
            closeHour = 21
        ),
        Place(
            id = "siberian_pancakes",
            name = "Siberian Pancakes",
            type = PlaceType.CAFE,
            point = Point(106, 107),
            description = "Блины и быстрый перекус на территории кампуса.",
            menuItems = listOf("pancakes", "snack", "tea", "dessert"),
            openHour = 9,
            closeHour = 20
        ),
        Place(
            id = "siberian_pancakes_2",
            name = "Siberian Pancakes",
            type = PlaceType.CAFE,
            point = Point(41, 41),
            description = "Блины и быстрый перекус",
            menuItems = listOf("pancakes", "snack", "tea", "dessert"),
            openHour = 11,
            closeHour = 21
        ),
        Place(
            id = "100Loveaya",
            name = "100LoveАЯ",
            type = PlaceType.CAFE,
            point = Point(99, 106),
            description = "Полноценные обеды и комплексное меню.",
            menuItems = listOf("full_meal", "coffee", "salad", "tea"),
            openHour = 9,
            closeHour = 17
        ),
        Place(
            id = "yarche",
            name = "Yarche",
            type = PlaceType.CAFE,
            point = Point(13, 137),
            description = "Магазин, где есть почти все",
            menuItems = listOf("snack", "disposable_tableware", "coffee", "water", "sandwich", "dessert"),
            openHour = 8,
            closeHour = 22
        ),
        Place(
            id = "Tochka_coffee",
            name = "Точка",
            type = PlaceType.CAFE,
            point = Point(141, 38),
            description = "Кофейная точка",
            menuItems = listOf("coffee"),
            openHour = 9,
            closeHour = 20
        ),
        Place(
            id = "XO_Bakery",
            name = "XO Bakery",
            type = PlaceType.CAFE,
            point = Point(121, 130),
            description = "Кафе с вкусным кофе",
            menuItems = listOf("coffee", "full_meal", "tea", "salad"),
            openHour = 9,
            closeHour = 18
        ),
        Place(
            id = "abricos",
            name = "Abricos",
            type = PlaceType.CAFE,
            point = Point(62, 194),
            description = "Магазин с напитками и быстрыми перекусами у юридического корпуса.",
            menuItems = listOf("snack", "coffee", "water", "sandwich", "dessert"),
            openHour = 8,
            closeHour = 21
        ),
        Place(
            id = "rostics",
            name = "Rostics",
            type = PlaceType.CAFE,
            point = Point(112, 32),
            description = "Фастфуд с быстрым обслуживанием.",
            menuItems = listOf("full_meal", "snack", "sandwich", "water"),
            openHour = 9,
            closeHour = 22
        ),
        Place(
            id = "baba_roma",
            name = "Baba Roma",
            type = PlaceType.CAFE,
            point = Point(72, 36),
            description = "Кафе-ресторан для обеда рядом с рощей.",
            menuItems = listOf("full_meal", "coffee", "snack", "tea", "dessert", "salad"),
            openHour = 10,
            closeHour = 21
        ),
        Place(
            id = "library_coworking",
            name = "Library Coworking",
            type = PlaceType.COWORKING,
            point = Point(142, 66),
            description = "Тихое место для учебы с розетками и столами.",
            openHour = 9,
            closeHour = 21
        ),

        Place(
            id = "vk_zone",
            name = "ВК зона",
            type = PlaceType.COWORKING,
            point = Point(119, 131),
            description = "ВК зона с мягкими диванчиками и приятной атмосферой",
            openHour = 8,
            closeHour = 20
        ),
        Place(
            id = "geophysical_center",
            name = "Символ геофизического центра Евразии",
            type = PlaceType.LANDMARK,
            point = Point(107, 58),
            description = "Камень, символ геофизического центра Евразии."
        ),
        Place(
            id = "florinsky_mendeleev",
            name = "Флоринский и Менделеев",
            type = PlaceType.LANDMARK,
            point = Point(110, 78),
            description = "Памятник В. М. Флоринскому и Д. И. Менделееву."
        ),
        Place(
            id = "I_love_Tomsk",
            name = "Я люблю Томск",
            type = PlaceType.LANDMARK,
            point = Point(7, 32),
            description = "Декоративная надпись"
        ),
        Place(
            id ="Bridge",
            name = "Мост через Медичку",
            type = PlaceType.LANDMARK,
            point = Point(70, 69),
            description = "Достопримечательность: мост через бывшую реку Медичку"
        ),
        Place(
            id ="Potanin",
            name = "Г.Н.Потанин",
            type = PlaceType.LANDMARK,
            point = Point(116, 60),
            description = "Памятник, мемориал, посвященный Г.Н.Потанину"
        ),
    )

    val cafes: List<Place>
        get() = places.filter { it.type == PlaceType.CAFE }

    val coworkings: List<Place>
        get() = places.filter { it.type == PlaceType.COWORKING }

    val landmarks: List<Place>
        get() = places.filter { it.type == PlaceType.LANDMARK }
}
