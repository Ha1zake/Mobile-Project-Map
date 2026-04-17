package ru.tsu.mobileprojectmap.domain.model

object SamplePlaces {

    val places = listOf(
        Place(
            id = "starbooks",
            name = "Starbooks",
            type = PlaceType.CAFE,
            point = Point(102, 114),
            description = "Кофе и напитки",
            menuItems = listOf("coffee"),
            openHour = 9,
            closeHour = 21
        ),
        Place(
            id = "siberian_pancakes",
            name = "Siberian Pancakes",
            type = PlaceType.CAFE,
            point = Point(106, 107),
            description = "Блины и перекус",
            menuItems = listOf("pancakes"),
            openHour = 9,
            closeHour = 20
        ),
        Place(
            id = "main_cafeteria",
            name = "Main Cafeteria",
            type = PlaceType.CAFE,
            point = Point(80, 105),
            description = "Полноценные обеды",
            menuItems = listOf("full_meal"),
            openHour = 8,
            closeHour = 18
        ),
        Place(
            id = "yarche",
            name = "Yarche",
            type = PlaceType.CAFE,
            point = Point(13, 137),
            description = "Магазин и перекус",
            menuItems = listOf("snack", "disposable_tableware"),
            openHour = 8,
            closeHour = 22
        ),
        Place(
            id = "library_coworking",
            name = "Library Coworking",
            type = PlaceType.COWORKING,
            point = Point(142, 66),
            description = "Тихое место для учебы",
            openHour = 9,
            closeHour = 20
        ),
        Place(
            id = "main_building",
            name = "Main Building",
            type = PlaceType.LANDMARK,
            point = Point(78, 72),
            description = "Главный корпус ТГУ"
        ),
        Place(
            id = "Abricos",
            name = "Abricos",
            type = PlaceType.CAFE,
            point = Point(62, 194),
            description = "Магазин Абрикос"
        ),
        Place(
            id = "Rostics",
            name = "Rostics",
            type = PlaceType.CAFE,
            point = Point(112, 32),
            description = "КФС"
        ),
        Place(
            id = "BabaRoma",
            name = "BabaRoma",
            type = PlaceType.CAFE,
            point = Point(72, 36),
            description = "Ресторан"
        )
    )
}