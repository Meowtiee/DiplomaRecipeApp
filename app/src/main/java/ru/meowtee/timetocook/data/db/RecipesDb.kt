package ru.meowtee.timetocook.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import ru.meowtee.timetocook.R
import ru.meowtee.timetocook.core.extensions.ioThread
import ru.meowtee.timetocook.data.converter.ListsConverter
import ru.meowtee.timetocook.data.dao.RecipesDao
import ru.meowtee.timetocook.data.dao.RecommendationsDao
import ru.meowtee.timetocook.data.db.RecipesDb.Companion.DATABASE_VERSION
import ru.meowtee.timetocook.data.model.Ingredient
import ru.meowtee.timetocook.data.model.Receipt
import ru.meowtee.timetocook.data.model.Recommendation

@Database(
    entities = [
        Receipt::class,
        Recommendation::class
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(ListsConverter::class)
abstract class RecipesDb : RoomDatabase() {
    abstract fun recommendationsDao(): RecommendationsDao
    abstract fun recipesDao(): RecipesDao

    companion object {
        const val DATABASE_VERSION = 11
        private const val DATABASE_NAME = "Recipes-Room"

        @Volatile
        private var INSTANCE: RecipesDb? = null

        fun getInstance(context: Context): RecipesDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDataSource(context).also { INSTANCE = it }
            }

        private fun buildDataSource(context: Context): RecipesDb =
            Room.databaseBuilder(context.applicationContext, RecipesDb::class.java, DATABASE_NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        ioThread {
                            PREPOPULATE_DATA_RECEIPTS.forEach {
                                getInstance(context).recipesDao().addRecipe(it)
                            }
                            PREPOPULATE_DATA_RECOMMENDATIONS.forEach {
                                getInstance(context).recommendationsDao().addRecommendation(it)
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        var recipes: List<Receipt>
                        var recommendations: List<Recommendation>
                        ioThread {
                            recipes = getInstance(context).recipesDao().getAllRecipes()
                            if (recipes.isEmpty()) {
                                PREPOPULATE_DATA_RECEIPTS.forEach {
                                    getInstance(context).recipesDao().addRecipe(it)
                                }
                            }

                            recommendations =
                                getInstance(context).recommendationsDao().getAllRecommendations()
                            if (recommendations.isEmpty()) {
                                PREPOPULATE_DATA_RECOMMENDATIONS.forEach {
                                    getInstance(context).recommendationsDao().addRecommendation(it)
                                }
                            }
                        }
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        runBlocking(Dispatchers.IO) {
                            getInstance(context).clearAllTables()
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()

        private val PREPOPULATE_DATA_RECEIPTS = listOf(
            Receipt(
                image = R.drawable.shakshuka,
                title = "Шакшука",
                isFavourite = false,
                portions = 2,
                type = "Завтрак",
                timeTag = "За 30 мин",
                rating = 3,
                time = "30 мин",
                ingredients = listOf(
                    Ingredient(
                        name = "Оливковое масло",
                        count = 1.0,
                        measure = "ст.л."

                    ),
                    Ingredient(
                        name = "Репчатый лук",
                        count = 0.5
                    ),
                    Ingredient(
                        name = "Чеснок",
                        count = 1.0,
                        measure = "зуб."
                    ),
                    Ingredient(
                        name = "Сладкий перец",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Помидоры",
                        count = 4.0
                    ),
                    Ingredient(
                        name = "Томатная паста",
                        count = 2.0,
                        measure = "ст.л."
                    ),
                    Ingredient(
                        name = "Молотый кумин",
                        count = 1.0,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Паприка",
                        count = 1.0,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Молотый черный перец",
                        count = 0.0,
                        measure = "По вкусу"
                    ),
                    Ingredient(
                        name = "Сахар",
                        count = 0.0,
                        measure = "По вкусу"
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.0,
                        measure = "По вкусу"
                    ),
                    Ingredient(
                        name = "Куриное яйцо",
                        count = 5.0
                    ),
                    Ingredient(
                        name = "Рубленная петрушка",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                difficult = "Простой",
                steps = mutableListOf(
                    "Разогрейте оливковое масло в глубокой сковороде. Добавьте лук, пассеруйте до прозрачности, добавьте чеснок и томите еще минуту-две",
                    "Добавьте порезанный болгарский перец – и готовьте еще 5–7 минут, после чего бросьте в сковороду помидоры и томатную пасту, и перемешайте. Теперь приправьте специями и сахаром. Снова перемешайте и снова готовьте 5–7 минут. Посолите, поперчите и добавьте приправы по вкусу",
                    "По одному выпустите в сковороду яйца, накройте крышкой и готовьте 10–15 минут",
                    "За это время соус чуть выпарится. Но следите за тем, чтобы он не выпарился совсем – иначе шакшука подгорит",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.kobler,
                title = "Коблер",
                portions = 6,
                time = "40 мин",
                type = "Завтрак",
                timeTag = "За 1 ч",
                ingredients = listOf(
                    Ingredient(
                        name = "Яблоки",
                        count = 5.0
                    ),
                    Ingredient(
                        name = "Финики",
                        count = 50.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Мед",
                        count = 2.0,
                        measure = "ст.л."
                    ),
                    Ingredient(
                        name = "Лимонный сок",
                        count = 1.0,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Ваниль",
                        count = 0.5,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.25,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Молотый имбирь",
                        count = 0.25,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Орехи",
                        count = 50.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Кокосовый масло",
                        count = 1.0,
                        measure = "ст.л."
                    ),
                ),
                steps = mutableListOf(
                    "Яблоки очистить, вынуть сердцевину и мелко порезать",
                    "Орехи перемолоть в комбайне. Добавить соль, ваниль, кокосовое масло и четверть стакана фиников. Перемолоть еще раз. Отдельно перемолоть оставшиеся финики со всеми остальными ингредиентами",
                    "В формы для запекания (лучше использовать порционные) выложить яблоки, затем — смесь фиников с медом, лимонным соком и имбирем. Перемешать. Сверху закрыть смесью с молотыми орехами",
                    "Запекать в духовке при 200 градусов около 30 минут до появления золотистого цвета",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.soup_cvetnaya_kapusta,
                title = "Суп-пюре из цветной капусты",
                isFavourite = false,
                portions = 6,
                time = "15 мин",
                timeTag = "за 30 мин",
                type = "Первое",
                difficult = "Продвинутый",
                ingredients = listOf(
                    Ingredient(
                        name = "Молотый черный перец",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Петрушка",
                        count = 20.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сливки 35%-ные",
                        count = 100.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Сливочное масло",
                        count = 100.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Куриный бульон",
                        count = 0.5,
                        measure = "л."
                    ),
                    Ingredient(
                        name = "Молоко",
                        count = 0.5,
                        measure = "л."
                    ),
                    Ingredient(
                        name = "Лук-порей",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Цветная капуста",
                        count = 1.0
                    ),
                ),
                steps = mutableListOf(
                    "Цветную капусту разобрать на соцветия, самые маленькие из них сварить целиком в подсоленной воде, а крупные порезать на более мелкие части",
                    "Как только маленькие соцветия сварятся, надо их тут же остудить, чтобы сохранить состояние аль-денте",
                    "Параллельно надо растопить в сотейнике или кастрюле сливочное масло и обжарить на нем мелкорубленый порей (только белую часть), не забывая все время перемешивать, чтобы не дать луку потемнеть",
                    "Влить в кастрюлю молоко, добавить нарезанные соцветия капусты и оставить вариться на небольшом огне в течение пяти минут. Молоко при этом не должно бурно кипеть, а лишь спокойно побулькивать. Через пять минут влить в кастрюлю горячий куриный бульон и варить, пока капуста не станет совсем мягкой",
                    "Снять с огня. Посолить, поперчить по вкусу, добавить мелко нарезанную петрушку, сливки и прокрутить суп в блендере. Подавать в глубокой тарелке, положив в нее маленькие похрустывающие соцветия цветной капусты и немного красной икры",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.tvor_zap,
                title = "Творожная запеканка в микроволновке",
                isFavourite = false,
                portions = 2,
                time = "10 мин",
                difficult = "Простой",
                ingredients = listOf(
                    Ingredient(
                        name = "Творог",
                        count = 220.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Овсяные хлопья",
                        count = 30.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Куриное яйцо",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Мед",
                        count = 1.0,
                        measure = "ст.л."
                    ),
                    Ingredient(
                        name = "Курага",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Корица",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "Смешайте в миске творог, яйцо, мед и овсяные хлопья до однородной массы. Мед можно заменить сахаром или сахарозаменителем",
                    "Добавьте нарезанную курагу и корицу. Перемешайте. При желании можно использовать другие сухофрукты и немного орехов",
                    "Поставьте готовую массу в микроволновку на 5 минут",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.potato_graten,
                title = "Картофельный гратен",
                isFavourite = false,
                portions = 8,
                timeTag = "За 1 ч",
                type = "Второе",
                time = "60 мин",
                difficult = "Сложный",
                ingredients = listOf(
                    Ingredient(
                        name = "Картофель",
                        count = 3.0
                    ),
                    Ingredient(
                        name = "Молоко",
                        count = 250.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Вода",
                        count = 250.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Чеснок",
                        count = 1.0,
                        measure = "зуб."
                    ),
                    Ingredient(
                        name = "Сливочное масло",
                        count = 50.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Твердый сыр",
                        count = 70.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сметана",
                        count = 150.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Мускатный орех",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Молотый черный перец",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "В маленькой кастрюльке соединить молоко с водой, посолить, поперчить и добавить порубленный зубчик чеснока. Довести до кипения",
                    "В это время помыть и почистить картофель. Порезать кружками толщиной 4–5 мм",
                    "Отправить картофель в кипящее молоко, чуть снизить температуру и варить 10–15 минут. Следить, чтобы кружки не разваливались. Слить картофельную воду",
                    "Форму для запекания смазать сливочным маслом. Выложить картофель. Смазать сметаной. Сверху выложить кусочки сливочного масла и посыпать сыром",
                    "Отправить в духовку на 40 минут и запекаться при 180 градусах",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.ruletiki,
                title = "Рулетики из баклажанов",
                isFavourite = false,
                difficult = "Простой",
                portions = 2,
                type = "Закуска",
                timeTag = "За 30 мин",
                time = "20 мин",
                ingredients = listOf(
                    Ingredient(
                        name = "Баклажаны",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Сыр",
                        count = 200.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Чеснок",
                        count = 3.0,
                        measure = "зуб."
                    ),
                    Ingredient(
                        name = "Майонез",
                        count = 100.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Помидоры",
                        count = 1.0
                    ),
                ),
                steps = mutableListOf(
                    "У баклажана отрезаем плодоножку и нарезаем вдоль на пластины. Обжариваем с двух сторон до готовности",
                    "Делаем начинку. Для этого смешиваем майонез, тертый сыр и чеснок",
                    "Каждую пластинку смазываем начинкой, кладем ломтик помидора и сворачиваем в рулет",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.potato_graten,
                title = "Ленивая овсянка в банке",
                isFavourite = false,
                portions = 1,
                timeTag = "За 30 мин",
                type = "Завтрак",
                time = "10 мин",
                difficult = "Простой",
                ingredients = listOf(
                    Ingredient(
                        name = "Овсяные хлопья",
                        count = 150.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Молоко",
                        count = 50.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Бананы",
                        count = 1.0
                    ),
                ),
                steps = mutableListOf(
                    "В маленькой кастрюльке соединить молоко с водой, посолить, поперчить и добавить порубленный зубчик чеснока. Довести до кипения",
                    "Добавляем фрукты, ягоды по вкусу, плотно закрываем банку и ставим в холодильник на ночь",
                    "Храниться такая овсянка может до 2 дней или даже больше, в зависимости от типа и зрелости плодов. За ночь овсяные хлопья пропитываются молоком, и фруктовыми соками. На следующий день каша уже будет мягкой и нежной",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.cheese_soup,
                title = "Сырный суп с охотничьими колбасками",
                isFavourite = false,
                portions = 4,
                timeTag = "За 1 ч",
                type = "Первое",
                time = "40 мин",
                difficult = "Сложный",
                ingredients = listOf(
                    Ingredient(
                        name = "Картофель",
                        count = 3.0
                    ),
                    Ingredient(
                        name = "Морковь",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Репчатый лук",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Охотничьи колбаски",
                        count = 150.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сливочное масло",
                        count = 15.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Вода",
                        count = 1.5,
                        measure = "л."
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 3.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Укроп",
                        count = 3.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Черный душистый перец",
                        count = 5.0
                    ),
                    Ingredient(
                        name = "Лавровый лист",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Плавленный сырок",
                        count = 2.0
                    ),
                ),
                steps = mutableListOf(
                    "Поставить кастрюлю с водой на плиту и включить ее. Пока кастрюля нагревается, заняться овощами. Картошку помыть, очистить и нарезать кубиками 7–10 мм, половину моркови помыть, очистить и нарезать мелкими кубиками, 3–5 мм. Отправить овощи в кастрюлю и подождать закипания",
                    "В это время поставить сковороду на плиту и приступить к очистке лука. Выложить сливочное масло на сковороду и приступить к нарезке лука мелкими кубиками. Выложить его на сковороду и жарить до прозрачности",
                    "Нарезать мелко колбаски и добавить обжаренный лук и колбаски в кастрюлю. Также добавить перец, лавровый лист и посолить",
                    "Когда овощи будут почти готовы, натереть плавленый сыр на мелкой терке (они так быстрее растворяются) прямо над кастрюлей и перемешать. Нужно контролировать огонь после перемешивания суп может убежать. Как только сыр полностью растворится, добавить мелко порезанную зелень, довести до кипения, Когда картофель будет готов, по вкусу посолить блюдо и выключить Можно дать немного настояться, минут 10",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.chiken_soup_mushroom,
                title = "Куриный суп с шампиньонами и зеленью",
                isFavourite = false,
                portions = 4,
                timeTag = "За 1,5 ч",
                type = "Первое",
                time = "80 мин",
                difficult = "Продвинутый",
                ingredients = listOf(
                    Ingredient(
                        name = "Картофель",
                        count = 3.0
                    ),
                    Ingredient(
                        name = "Куриная грудка",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Шампиньоны",
                        count = 400.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сливочное масло",
                        count = 3.0,
                        measure = "ст.л."
                    ),
                    Ingredient(
                        name = "Репчатый лук",
                        count = 0.5
                    ),
                    Ingredient(
                        name = "Петрушка",
                        count = 0.5,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Морковь",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Лавровый лист",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Молотый черный перец",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "Варим куриный бульон (посолить по вкусу). Нарезаем шампиньоны пластинками, но не очень тонко, чтобы не подгорели. Тушим их со сливочным маслом до готовности. Пока тушатся грибы чистим и нарезаем картофель кубиками 0.5 см",
                    "Когда бульон готов, достаем курицу и засыпаем в него готовые грибы. Варим минут 10-15 на среднем огне. После грибов (через 10-15 минут) добавляем картофель",
                    "Порезать лук и натереть морковь на терке. Потушить смесь пока лук не приобретет золотистый оттенок. Добавляем тушеную смесь в бульон. Пока все это дело варится, режем курицу небольшими кубиками или соломкой. Засыпаем обратно в бульон",
                    "Кладем лавровый лист. Мелко шинкуем петрушку и добавляем в готовый суп",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.pumpkin_soup,
                title = "Суп из тыквы",
                isFavourite = false,
                portions = 4,
                timeTag = "За 1 ч",
                type = "Первое",
                time = "60 мин",
                difficult = "Простой",
                ingredients = listOf(
                    Ingredient(
                        name = "Тыква",
                        count = 1.0,
                        measure = "кг."
                    ),
                    Ingredient(
                        name = "Красный лук",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Чеснок",
                        count = 4.0,
                        measure = "зуб."
                    ),
                    Ingredient(
                        name = "Куриный бульон",
                        count = 1.0,
                        measure = "л."
                    ),
                    Ingredient(
                        name = "Бренди",
                        count = 100.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Сапар",
                        count = 2.0,
                        measure = "ст.л."
                    ),
                    Ingredient(
                        name = "Петрушка",
                        count = 20.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сливочное масло",
                        count = 50.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Оливковое масло",
                        count = 20.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сливки 35%-ные",
                        count = 100.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Молотый черный перец",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "Нарезать кубиками мякоть тыквы, красный лук и чеснок. Растопить сливочное масло в сотейнике, добавить оливковое масло и обжарить лук. Добавить чеснок и жарить на среднем огне до появления чесночного запаха",
                    "Добавить к содержимому сотейника тыкву, затем всыпать сахар, влить бренди и тушить овощи 3 минуты, постоянно помешивая",
                    "Добавить щепотку соли, залить куриным бульоном и довести до кипения. Залить готовую смесь сливками, посолить и поперчить по вкусу и подавать, украсив петрушкой",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.paprikash,
                title = "Паприкаш со свининой",
                isFavourite = false,
                portions = 6,
                timeTag = "За 1,5 ч",
                type = "Второе",
                time = "90 мин",
                difficult = "Сложный",
                ingredients = listOf(
                    Ingredient(
                        name = "Картофель",
                        count = 10.0
                    ),
                    Ingredient(
                        name = "Лук",
                        count = 3.0
                    ),
                    Ingredient(
                        name = "Помидоры",
                        count = 3.0
                    ),
                    Ingredient(
                        name = "Морковь",
                        count = 2.0
                    ),
                    Ingredient(
                        name = "Сладкий перец",
                        count = 3.0
                    ),
                    Ingredient(
                        name = "Сладкая паприка",
                        count = 1.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сметана",
                        count = 250.0,
                        measure = "ст.л."
                    ),
                    Ingredient(
                        name = "Мука",
                        count = 1.0,
                        measure = "ст.л"
                    ),
                    Ingredient(
                        name = "Чеснок",
                        count = 3.5,
                        measure = "зуб."
                    ),
                    Ingredient(
                        name = "Растительное масло",
                        count = 50.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Свиное филе",
                        count = 600.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Петрушка",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Укроп",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Молотый черный перец",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "Вымытую и хорошо обсушенную свинину нарезаем кусочками среднего размера. Вычищенные перцы превращаем в тонкую лапшу, помидоры режем дольками. Лук мелко шинкуем, измельчаем чеснок, морковь — на терке бурачной",
                    "Если собираемся жарим на топленом сале, раскаляем его на сильном огне в казанке, сотейнике или просто в сковороде с высокими бортиками. Второй вариант — нарезать сало тонкими ломтиками и вытопить его. Третий — жарить на растительном масле",
                    "В любом случае в раскаленное масло высыпаем нашинкованный лук и морковь, жарим, помешивая, несколько минут, добавляем кусочки свинины и, так же помешивая и переворачивая, жарим 10–15 минут. Посыпаем паприкой и чесноком",
                    "Через 2–3 минуты добавляем сладкий перец и помидоры. Даем закипеть, переводим огонь на малый и оставляем паприкаш тушиться под крышкой до готовности, что обычно занимает 20–25 минут",
                    "Самое время поставить вариться картофель, с которым подадим паприкаш. А если добавить еще и мучные клецки — это будет совсем по-венгерски. Хотя подойдет и отварной рис — дело вкуса",
                    "Сметану взбиваем с мукой, выливаем в казан и даем покипеть еще 4–5 минут. Снимаем с огня, заправляем солью и перцем",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.chiken_cheese,
                title = "Куриная грудка в сырном кляре",
                isFavourite = false,
                portions = 4,
                timeTag = "За 30 мин",
                type = "Второе",
                time = "30 мин",
                difficult = "Продвинутый",
                ingredients = listOf(
                    Ingredient(
                        name = "Куриная грудка",
                        count = 500.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сыр",
                        count = 250.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Куриные яйца",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Майонез",
                        count = 150.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Чеснок",
                        count = 6.0,
                        measure = "зуб."
                    ),
                    Ingredient(
                        name = "Мука",
                        count = 200.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Молотый черный перец",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "Куриную грудку отбить, посолить и поперчить. Одну грудку порезать на 2–3 части",
                    "Сыр потереть на крупной или мелкой терке. Тертый сыр смешать с сырым яйцом, майонезом и измельченным чесноком",
                    "Кусочки куриной грудки поместить в приготовленную смесь, перемешать",
                    "Достать из смеси по одному кусочку куриной грудки, обвалять его в муке и жарить до появления золотистой корочки",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.kyskys,
                title = "Кускус с курицей и овощами",
                isFavourite = false,
                portions = 6,
                timeTag = "За 30 мин",
                type = "Второе",
                time = "30 мин",
                difficult = "Продвинутый",
                ingredients = listOf(
                    Ingredient(
                        name = "Куриные бедра",
                        count = 6.0
                    ),
                    Ingredient(
                        name = "Лук",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Сладкий перец",
                        count = 4.0
                    ),
                    Ingredient(
                        name = "Помидоры",
                        count = 4.0
                    ),
                    Ingredient(
                        name = "Кускус",
                        count = 350.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Лимон",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Оливковое масло",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "В глубокой кастрюле (более оснащенные могут воспользоваться тажином) обжарить на оливковом масле куриные бедра до золотистой корочки",
                    "Вынуть курицу и в ее жиру обжарить мелконарезанный лук, потом то же самое проделать с измельченным болгарским перцем",
                    "Ошпарить помидоры, снять кожицу и мелко нарубить мякоть. Добавить в кастрюлю и тоже обжарить. Бросить обратно курицу и немного потушить все вместе",
                    "Добавить кускус и, при необходимости, перемешать (впрочем, это необязательно). Закрыть крышкой и оставить до готовности",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.cheburek,
                title = "Чебуреки с капустой",
                isFavourite = false,
                portions = 10,
                timeTag = "За 1 ч",
                type = "Закуска",
                time = "60 мин",
                difficult = "Сложный",
                ingredients = listOf(
                    Ingredient(
                        name = "Вода",
                        count = 0.5,
                        measure = "л."
                    ),
                    Ingredient(
                        name = "Мука",
                        count = 800.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Репчатый лук",
                        count = 2.0
                    ),
                    Ingredient(
                        name = "Белокачанная капуста",
                        count = 1.0,
                        measure = "кг."
                    ),
                    Ingredient(
                        name = "Морковь",
                        count = 2.0
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Молотый черный перец",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "В горячей воде растворить соль, соединить с просеянной мукой и замесить крутое тесто. Тесто накрыть полотенцем или салфеткой и оставить на 15–20 минут. Затем достаем тесто, тонко раскатываем и вырезаем кружки диаметром 15 см",
                    "Лук мелко нарезать. Морковь натереть на крупной терке. Капусту тонко нашинковать",
                    "Обжарить лук с морковью, затем добавить капусту и тушить до мягкости капусты. Посолить и поперчить",
                    "На одну половину лепешки положить капусту и накрыть другой половиной лепешки, края хорошо защипать — получится пирожок в форме полумесяца",
                    "Жарить чебуреки в растительном масле с двух сторон до готовности",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.mors,
                title = "Черничный морс",
                isFavourite = false,
                portions = 3,
                timeTag = "За 1 ч",
                type = "Напиток",
                time = "40 мин",
                difficult = "Продвинутый",
                ingredients = listOf(
                    Ingredient(
                        name = "Черника",
                        count = 200.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Вода",
                        count = 1.0,
                        measure = "л."
                    ),
                    Ingredient(
                        name = "Сахар",
                        count = 180.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Ванильный сахар",
                        count = 2.0,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Молотая корица",
                        count = 0.25,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Сок",
                        count = 1.0,
                        measure = "л."
                    )
                ),
                steps = mutableListOf(
                    "Чернику протрите и отожмите из нее сок. Мезгу залейте водой, добавьте сахар, ванильный сахар, корицу и варите 15 минут",
                    "Отвар процедите, затем влейте сок. Подавайте морс охлажденным",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.konvertiki,
                title = "Конвертики с курицей и сыром",
                isFavourite = false,
                portions = 8,
                timeTag = "За 1 ч",
                type = "Закуска",
                time = "60 мин",
                difficult = "Простой",
                ingredients = listOf(
                    Ingredient(
                        name = "Лаваш",
                        count = 2.0
                    ),
                    Ingredient(
                        name = "Копченая курица",
                        count = 300.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Рубленная петрушка",
                        count = 50.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Зелень",
                        count = 100.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Твердый сыр",
                        count = 300.0,
                        measure = "г."
                    ),
                ),
                steps = mutableListOf(
                    "Грудку, зелень мелко нарезать. Сыр натереть на мелкой терке. Все смешать",
                    "Лаваш нарезать на квадратики. В каждый квадратик положить полученную смесь, завернуть как конвертик",
                    "Разогреть сковородку с растительным маслом. Выложить конвертики на сковородку так, чтобы не развернулись. Обжарить с одной стороны, перевернуть. Выложить готовые конвертики на тарелку",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.lobio,
                title = "Лобио",
                isFavourite = false,
                portions = 4,
                timeTag = "Более 1,5 ч",
                type = "Закуска",
                time = "150 мин",
                difficult = "Сложный",
                ingredients = listOf(
                    Ingredient(
                        name = "Кинза",
                        count = 10.0
                    ),
                    Ingredient(
                        name = "Молотый кориандр",
                        count = 2.0,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Красная фасоль",
                        count = 800.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Чеснок",
                        count = 2.0,
                        measure = "зуб."
                    ),
                    Ingredient(
                        name = "Репчатый лук",
                        count = 4.0
                    ),
                    Ingredient(
                        name = "Винный уксус",
                        count = 2.0,
                        measure = "ст.л."
                    ),
                    Ingredient(
                        name = "Растительное масло",
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "Замачиваем фасоль. Чем дольше она пролежит в воде, тем нежнее будет лобио, поэтому лучше замочить фасоль с вечера",
                    "Промываем фасоль, кладем ее в кастрюлю, заливаем водой и ставим на огонь. Через пятнадцать минут после того как она закипит, сливаем воду",
                    "Снова заливаем фасоль водой и варим до готовности – минимум полтора-два часа. Хотя некоторые варят и по пять часов, периодически добавляя воду. Если фасолину можно растереть пальцами – она готова",
                    "Пока варится фасоль, мелко режем лук и жарим его в растительном масле до золотистого цвета",
                    "Толчем фасоль в кастрюле до тех пор, пока каждая вторая фасолина не будет размята. Считать, конечно, фасолины не надо – просто прикинуть на глаз. Смешиваем фасоль с луком, мелко нарезанной кинзой, толченым чесноком, кориандром и винным уксусом. Солим. Подаем горячим",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.milkshake,
                title = "Арахисовый милкшейк",
                isFavourite = false,
                portions = 1,
                timeTag = "За 30 мин",
                type = "Напиток",
                time = "30 мин",
                difficult = "Простой",
                ingredients = listOf(
                    Ingredient(
                        name = "Молоко",
                        count = 0.25,
                        measure = "л."
                    ),
                    Ingredient(
                        name = "Арахисовое масло",
                        count = 2.0,
                        measure = "ст.л."
                    ),
                    Ingredient(
                        name = "Мед",
                        count = 1.0,
                        measure = "ст.л."
                    ),
                    Ingredient(
                        name = "Ванильное мороженое",
                        count = 8.0,
                        measure = "ст.л."
                    ),
                ),
                steps = mutableListOf(
                    "В блендере смешать молоко, арахисовое масло и мед",
                    "Перемешать до однородной массы и добавить мороженое",
                    "Снова перемешать и вылить в высокий стакан",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.lemonade,
                title = "Домашний лимонад",
                isFavourite = false,
                portions = 6,
                timeTag = "За 30 мин",
                type = "Напиток",
                time = "15 мин",
                difficult = "Простой",
                ingredients = listOf(
                    Ingredient(
                        name = "Сахар",
                        count = 150.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Лимонный сок",
                        count = 15.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Вода",
                        count = 0.8,
                        measure = "л."
                    ),
                    Ingredient(
                        name = "Лимон",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Лед",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "В графине смешать лимонный сок и воду",
                    "Добавить сахар и перемешивать до полного растворения",
                    "Добавить лимон, нарезанный тонкими кусочками, и лед",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.hot_chocolate,
                title = "Мятный горячий шоколад",
                isFavourite = false,
                portions = 1,
                timeTag = "За 1,5 ч",
                type = "Напиток",
                time = "70 мин",
                difficult = "Сложный",
                ingredients = listOf(
                    Ingredient(
                        name = "Молоко",
                        count = 250.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Какао",
                        count = 2.0,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Сахар",
                        count = 1.0,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Горький шоколад",
                        count = 50.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Свежая мята",
                        count = 5.0
                    ),
                ),
                steps = mutableListOf(
                    "В небольшом сотейнике вскипятите молоко, утопите в нём порванные на кусочки мятные листья, снимите сотейник с огня, накройте крышкой и оставьте настояться на 1 час",
                    "Процедите молоко сквозь сито и перед тем как выкидывать мяту, хорошенько её отожмите, чтобы она отдала молоку как можно больше своего аромата и эфирных масел",
                    "Верните процеженное молоко в сотейник, верните сотейник на огонь, добавьте какао-порошок и сахар, варите 1-2 минуты до готовности",
                    "Снимите с огня, добавьте темный шоколад, оставьте смесь на минуту, после чего перемешайте до однородности и немного взбейте напиток венчиком ради небольшого намека на пенку",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.keks,
                title = "Кекс с черешней",
                isFavourite = false,
                portions = 4,
                timeTag = "За 30 мин",
                type = "Десерт",
                time = "30 мин",
                difficult = "Продвинутый",
                ingredients = listOf(
                    Ingredient(
                        name = "Мука",
                        count = 40.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сахарная пудра",
                        count = 20.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сахар",
                        count = 200.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Лимонный сок",
                        count = 1.0,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Куриные яйца",
                        count = 3.0
                    ),
                    Ingredient(
                        name = "Разрыхлитель",
                        count = 0.5,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Сливочное масло",
                        count = 100.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Молоко",
                        count = 60.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Черешня",
                        count = 750.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Корица",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "Масло и сахар взбить до рыхлой смеси. Ввести в смесь яйца, молоко, лимонный сок, муку с разрыхлителем",
                    "Смазать форму маслом и присыпать мукой или крошками",
                    "Выложить смесь в смазанную маслом и посыпанную мукой форму. На тесто положить очищенные от косточек черешни",
                    "Сверху посыпать рассыпчатой смесью, приготовленной из муки (40 г), сахара (20 г), корицы и разогретого масла (20 г). Выпекать на умеренно сильном жару",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.bulochki,
                title = "Булочки с корицей",
                isFavourite = true,
                portions = 12,
                timeTag = "Более 1,5 ч",
                type = "Десерт",
                time = "120 мин",
                difficult = "Сложный",
                ingredients = listOf(
                    Ingredient(
                        name = "Молоко",
                        count = 200.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Сухие дрожжи",
                        count = 11.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Мука",
                        count = 0.85,
                        measure = "кг."
                    ),
                    Ingredient(
                        name = "Куриные яйца",
                        count = 2.0
                    ),
                    Ingredient(
                        name = "Сливочное масло",
                        count = 200.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Тростниковый сахар",
                        count = 200.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Ванильный сахар",
                        count = 10.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сахар",
                        count = 100.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Сахарная пудра",
                        count = 100.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Корица",
                        count = 20.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Соль",
                        count = 1.0,
                        measure = "ч.л."
                    ),
                    Ingredient(
                        name = "Сливочный сыр",
                        count = 100.0,
                        measure = "г."
                    ),
                ),
                steps = mutableListOf(
                    "Дрожжи развести в теплом молоке (плюс добавляем чуточку сахара (из начальных 100 граммов добавить). Пока дрожжи поднимаются, в отдельной миске взбить яйца. К яйцам добавить размягченное масло (1/3 от пачки). В яично-масляную смесь добавить сахар (оставшийся из начальных 100 граммов)",
                    "Смешать разведенные в молоке дрожжи со смесью из яйца, масла и сахара, тщательно перемешать в посуде, где в дальнейшем будет замешиваться тесто. Муку (600–700 граммов (сколько тесто возьмет, на самом деле) соединить с солью. Часть смеси (муки с солью) всыпать в дрожжевое тесто и вымесить его. Сделать клейковину (чайная ложка муки + чайная ложка воды смешать, слепить шарик и промыть холодной водой). Выложить получившийся шарик в тесто, перемешать",
                    "После добавления клейковины добавить оставшуюся смесь из муки и соли, вымесить тесто. Накрыть получившийся шарик полотенцем и постаивить в теплое место на час. В это время подготовить начинку. В микроволновой печи размягчить масло (секунд 7–10). Соединяем корицу и тростниковый сахар",
                    "Приступить к «сборке». Необходимо раскатать полученное тесто, которое уже должно было увеличиться вдвое, в тонкий большой пласт, желательно прямоугольный (примерно 40 см на 55 см). Растопленное масло намазать на пласт. Посыпать пласт смесью корицы и тростникового сахара. Скатать пласт в тугой рулет (чем туже, тем лучше)",
                    "Разрезать рулет на кругляши – синнабоны. Резать можно ножом или ниткой (ниткой лучше). Получившиеся синнабоны уложить в форму для выпекания, застеленную пекарской бумагой и смазанную сливочным маслом. Постаивить в духовку на 20–30 минут при 175 градусах (пока не станут золотистого цвета; затем еще сверху их чуть подпекти, чтобы сахар растворился)",
                    "После того, как булочки подрумянились и готовы, вынуть их из духовки и сразу покрыть сливочной смесью (можно смазать кисточкой, а можно и залить)",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.sloiki,
                title = "Слойки с яблоком и тыквой",
                isFavourite = false,
                portions = 4,
                timeTag = "За 30 мин",
                type = "Десерт",
                time = "30 мин",
                difficult = "Продвинутый",
                ingredients = listOf(
                    Ingredient(
                        name = "Куриные яйца",
                        count = 1.0
                    ),
                    Ingredient(
                        name = "Слоеное тесто",
                        count = 500.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Молоко",
                        count = 10.0,
                        measure = "мл."
                    ),
                    Ingredient(
                        name = "Яблоко",
                        count = 1.0,
                        measure = "кг."
                    ),
                    Ingredient(
                        name = "Тыква",
                        count = 300.0,
                        measure = "г"
                    ),
                    Ingredient(
                        name = "Мед",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Сахар",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Сливочное масло",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "корица",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "Размораживаем тесто. Подготавливаем начинку. Кусочками нарезать тыкву, яблоки и потушим в меде до мягкости",
                    "Тесто режем на прямоугольнички (размер — на ваш вкус), на одной половинке делаем надрезы, на вторую выкладываем начинку (побольше). Далее складываем пополам (т.е. частью с надрезами накрываем сверху начинку) и вилкой/рукой закрепляем",
                    "Я обычно мажу сверху смесью желток + молоко/обсыпаю сверху сахаром чуть и оставляю постоять минут 10",
                    "Выкладываю на противень и отправляю в духовку при 180 градусах на 20 минут (тут стоит ориентироваться на свою духовку, начинка и так готова, а тесто не очень долго печется)",
                    "Рецепт готов!"
                )
            ),
            Receipt(
                image = R.drawable.sherbet,
                title = "Ягодный щербет",
                isFavourite = false,
                portions = 2,
                timeTag = "За 30 мин",
                type = "Десерт",
                time = "10 мин",
                difficult = "Простой",
                ingredients = listOf(
                    Ingredient(
                        name = "Замороженная клубника",
                        count = 200.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Курага",
                        count = 55.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Замороженная вишня",
                        count = 200.0,
                        measure = "г."
                    ),
                    Ingredient(
                        name = "Мед",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Финики",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                    Ingredient(
                        name = "Лимонный сок",
                        count = 0.0,
                        measure = "по вкусу"
                    ),
                ),
                steps = mutableListOf(
                    "Промойте замороженные ягоды под холодной водой",
                    "Смешайте ягоды и курагу в блендере до однородной массы",
                    "Для большей сладости можно добавить ложку меда или несколько фиников. Для кислинки — лимонный сок. Вместо клубники можно добавить мороженый ананас",
                    "Рецепт готов!"
                )
            ),
        )
        val PREPOPULATE_DATA_RECOMMENDATIONS = listOf(
            Recommendation(
                title = "Хорошо прогревайте сковороду",
                description = "Если не прогревать сковороду, то продукты будут либо пригорать, либо больше тушиться\n" +
                        "Капните на разогретую сковороду воды и если она мгновенно испарилась, то сковорода достаточно горячая"
            ),
            Recommendation(
                title = "Варите овощи по таймеру",
                description = "Речь идет о брокколи, спарже и других зеленых овощах\n" +
                        "Необходимо варить 3-7 минут в кипящей воде, а затем – самое важное – переложить овощи в кастрюлю с ледяной водой. Т.е.  “бланшировать” овощи"
            ),
            Recommendation(
                title = "Тушите на медленном огне",
                description = "Жидкости должно быть столько, чтобы покрывать ингредиенты не более чем на половину\n" +
                        "После этого убавьте “огонь” на плите. Мелкие пузырьки могут появляться не чаще раза в 2-3 секунды, иначе уменьшите огонь"
            ),
            Recommendation(
                title = "Не торопитесь с мясом",
                description = "Перед готовкой, пусть мясо приобретет комнатную температуру, тогда оно приготовится более равномерно\n" +
                        "После готовки мясу надо полежать 5-15 минут и только после подавать к столу"
            ),
            Recommendation(
                title = "Убирайте влагу с овощей",
                description = "После промывки овощей хорошо обтереть от влаги и после нарезать\n" +
                        "После сливания жидкости из банки опрокиньте овощи в дуршлаг и оставьте сушиться на 10-15 минут"
            ),
            Recommendation(
                title = "Учитывайте условия хранения",
                description = "В холодильнике не нужно хранить большинство овощей и фруктов, таких как киви, помидоры, зелень, бананы и многое другое. То же касается меда, варенья и консервов"
            ),
        )
    }
}