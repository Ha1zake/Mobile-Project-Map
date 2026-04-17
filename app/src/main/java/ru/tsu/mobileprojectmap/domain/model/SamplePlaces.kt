package ru.tsu.mobileprojectmap.domain.model

object SamplePlaces {

    val places = listOf(
        Place(
            id = "starbooks",
            name = "Starbooks",
            type = PlaceType.CAFE,
            point = Point(102, 114),
            description = "Кофе, десерты и напитки рядом с учебными корпусами.",
            menuItems = listOf("coffee", "snack"),
            openHour = 9,
            closeHour = 21
        ),
        Place(
            id = "siberian_pancakes",
            name = "Siberian Pancakes",
            type = PlaceType.CAFE,
            point = Point(106, 107),
            description = "Блины и быстрый перекус на территории кампуса.",
            menuItems = listOf("pancakes", "snack"),
            openHour = 9,
            closeHour = 20
        ),
        Place(
            id = "main_cafeteria",
            name = "Main Cafeteria",
            type = PlaceType.CAFE,
            point = Point(80, 105),
            description = "Полноценные обеды и комплексное меню.",
            menuItems = listOf("full_meal", "coffee"),
            openHour = 8,
            closeHour = 18
        ),
        Place(
            id = "yarche",
            name = "Yarche",
            type = PlaceType.CAFE,
            point = Point(13, 137),
            description = "Магазин для перекуса и покупки одноразовой посуды.",
            menuItems = listOf("snack", "disposable_tableware", "coffee"),
            openHour = 8,
            closeHour = 22
        ),
        Place(
            id = "bus_stop_coffee",
            name = "Bus Stop Coffee",
            type = PlaceType.CAFE,
            point = Point(28, 154),
            description = "Кофейная точка у остановки.",
            menuItems = listOf("coffee", "snack"),
            openHour = 8,
            closeHour = 19
        ),
        Place(
            id = "second_building_cafe",
            name = "Second Building Cafe",
            type = PlaceType.CAFE,
            point = Point(118, 87),
            description = "Небольшое кафе рядом со вторым корпусом.",
            menuItems = listOf("coffee", "full_meal"),
            openHour = 9,
            closeHour = 18
        ),
        Place(
            id = "abricos",
            name = "Abricos",
            type = PlaceType.CAFE,
            point = Point(62, 194),
            description = "Магазин с напитками и быстрыми перекусами у выхода с кампуса.",
            menuItems = listOf("snack", "coffee"),
            openHour = 8,
            closeHour = 21
        ),
        Place(
            id = "rostics",
            name = "Rostics",
            type = PlaceType.CAFE,
            point = Point(112, 32),
            description = "Фастфуд с быстрым обслуживанием.",
            menuItems = listOf("full_meal", "snack"),
            openHour = 9,
            closeHour = 22
        ),
        Place(
            id = "baba_roma",
            name = "Baba Roma",
            type = PlaceType.CAFE,
            point = Point(72, 36),
            description = "Кафе для спокойного обеда рядом с рощей.",
            menuItems = listOf("full_meal", "coffee", "snack"),
            openHour = 10,
            closeHour = 21
        ),
        Place(
            id = "library_coworking",
            name = "Library Coworking",
            type = PlaceType.COWORKING,
            point = Point(142, 66),
            description = "Тихое место для учёбы с розетками и столами.",
            openHour = 9,
            closeHour = 20
        ),
        Place(
            id = "north_reading_room",
            name = "North Reading Room",
            type = PlaceType.COWORKING,
            point = Point(132, 82),
            description = "Читальный зал для индивидуальной и групповой работы.",
            openHour = 9,
            closeHour = 19
        ),
        Place(
            id = "student_coworking",
            name = "Student Coworking",
            type = PlaceType.COWORKING,
            point = Point(54, 92),
            description = "Общая зона для командной работы студентов.",
            openHour = 8,
            closeHour = 21
        ),
        Place(
            id = "green_hall",
            name = "Green Hall",
            type = PlaceType.COWORKING,
            point = Point(91, 58),
            description = "Свободная аудитория с мягкой посадкой.",
            openHour = 8,
            closeHour = 18
        ),
        Place(
            id = "main_building",
            name = "Main Building",
            type = PlaceType.LANDMARK,
            point = Point(78, 72),
            description = "Главный корпус ТГУ."
        ),
        Place(
            id = "science_library",
            name = "Science Library",
            type = PlaceType.LANDMARK,
            point = Point(138, 73),
            description = "Научная библиотека и одна из ключевых точек кампуса."
        ),
        Place(
            id = "university_grove",
            name = "University Grove",
            type = PlaceType.LANDMARK,
            point = Point(91, 111),
            description = "Центральная прогулочная часть университетской рощи."
        ),
        Place(
            id = "main_gate",
            name = "Main Gate",
            type = PlaceType.LANDMARK,
            point = Point(36, 166),
            description = "Главный вход на территорию кампуса."
        ),
        Place(
            id = "botanical_alley",
            name = "Botanical Alley",
            type = PlaceType.LANDMARK,
            point = Point(118, 52),
            description = "Аллея для прогулок с видом на корпуса и рощу."
        )
    )

    val cafes: List<Place>
        get() = places.filter { it.type == PlaceType.CAFE }

    val coworkings: List<Place>
        get() = places.filter { it.type == PlaceType.COWORKING }

    val landmarks: List<Place>
        get() = places.filter { it.type == PlaceType.LANDMARK }
}
