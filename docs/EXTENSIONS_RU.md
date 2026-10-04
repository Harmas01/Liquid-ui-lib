# Расширения Liquid UI — API 1.1

## Подключение

Fabric 1.20.1, Java 17, Fabric API. Примеры используют Mojang mappings:
`mappings loom.officialMojangMappings()`. В Yarn названия классов Minecraft отличаются.
Соберите библиотеку и скопируйте релизный `liquid-ui-library-1.1.0.jar` в `libs` проекта.

```groovy
dependencies {
    modImplementation files('libs/liquid-ui-library-1.1.0.jar')
}
```

В `fabric.mod.json` задайте клиентское окружение и зависимость
`"liquid_ui_library": ">=1.1.0 <2.0.0"`. Также нужны зависимости Minecraft, Java,
Fabric Loader и Fabric API. Полный файл есть в [готовом примере](../../examples/hello-liquid-ui).
JAR библиотеки устанавливается пользователем отдельно, как Fabric API.

## Добавить плитку

В `ClientModInitializer.onInitializeClient()`:

```java
LiquidMenus.registerScreen(new ResourceLocation("my_mod", "settings"),
        Component.literal("Мои настройки"), 100, MyScreen::new);
```

`MyScreen` принимает `Screen parent` в конструкторе. Фабрика вызывается при нажатии.
Для произвольного действия есть `LiquidMenus.register(id, label, order, parent -> ...)`.
ID уникален и содержит namespace вашего мода; повторный ID вызывает понятную ошибку.
Регистрируйте один раз при инициализации клиента, не из render/init экрана.
Меньший order идёт раньше среди зарегистрированных плиток, при равенстве сохраняется
порядок регистрации. Поддерживаются переводимые подписи `Component.translatable`.

LoadingDisplay 1.1.0+ размещает плитки после стандартных кнопок. Для такого расширения
добавьте зависимость `"loadingdisplay": ">=1.1.0 <2.0.0"` и его релизный JAR через
`modRuntimeOnly files('libs/loadingdisplay-1.1.0.jar')`. Библиотека сама меню не меняет.
Другой хост может вызвать `LiquidMenus.createButtons(parent)` один раз при инициализации
и добавить/разместить полученные кнопки. Для собственных экранов LoadingDisplay не нужен.

## Стеклянный экран

Наследуйте `GlassScreen`, передайте в super заголовок и родительский экран.
В `init()` добавляйте кнопки:

```java
addRenderableWidget(GlassButton.glassBuilder(Component.literal("Назад"), b -> onClose())
        .position(width / 2 - 100, height / 2).size(200, 48)
        .theme(LiquidTheme.builder().opacity(0.8F).hoverLift(4).build()).build());
```

`GlassScreen` рисует фон, захватывает его для размытия, затем рисует содержимое и кнопки.
Фон меняйте в `renderGlassBackground`, надписи — в `renderGlassContent`. Метод render
закрыт для переопределения, чтобы сохранить правильный порядок. Escape и `onClose()`
возвращают родительский экран. Не вызывайте release при каждом переходе: ресурсы общие.

## Настройки темы и другие классы

| Метод builder темы | Назначение |
| --- | --- |
| opacity(0..1) | Прозрачность |
| hoverLift(int) | Подъём при наведении в GUI-пикселях |
| focusGrowth(int) | Увеличение при выборе клавиатурой |
| animationSpeed(0.01..1) | Скорость приближения к состоянию за кадр |

`GlassPanel.capture/draw` позволяют встроить панель в обычный Screen: захватите фон
один раз за кадр **после фона, до текста и стекла**. `DotLoadingRenderer.drawFullScreen`
рисует белый фон с точками, `ExitLoadingRenderer.draw` — анимацию выхода.
Радиус и сила размытия пока задаются общим шейдером. Индивидуальных параметров темы
для них и готовой кнопки с иконкой в API 1.1 нет.

## Совместимость

API 1.1 сохраняет прежние классы и добавляет GlassScreen/LiquidMenus. Новым расширениям
нужна версия >=1.1.0. Политика дальнейших версий: дополнения без слома сигнатур — 1.x,
удаление/изменение публичных сигнатур — 2.0. Это не гарантия работы с чужими mixin.
Все операции с UI выполняйте на клиентском потоке. Зависимости должны быть клиентскими.
Регистрировать плитки после инициализации клиента в API 1.1 не предусмотрено.
