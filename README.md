# Liquid UI Library

**API 1.2:** [Руководство по Java-расширениям](docs/EXTENSIONS_RU.md) ·
[Интерфейсы без кода](docs/NO_CODE_RU.md) ·
[Готовый отдельный Fabric-проект](../examples/hello-liquid-ui).
Доступны `GlassScreen` для экранов и `LiquidMenus` для регистрации плиток без mixin.

Клиентская Fabric-библиотека для Minecraft 1.20.1. Она предоставляет жидкое стекло,
скруглённые панели и кнопки, анимации наведения и готовые точечные загрузчики.

## Подключение

Добавьте JAR библиотеки в `mods` и укажите обязательную зависимость:

```json
"depends": {
  "liquid_ui_library": ">=1.2.0 <2.0.0"
}
```

Перед отрисовкой стеклянных элементов один раз за кадр захватите фон:

```java
GlassPanel.capture(graphics);
GlassPanel.draw(graphics, 20, 20, 220, 100, 0.85F);
```

Кнопка для собственного `Screen`:

```java
addRenderableWidget(GlassButton.glassBuilder(Component.literal("Настройки"), button -> openOptions())
        .position(20, 20)
        .size(160, 52)
        .theme(LiquidTheme.builder().opacity(0.75F).hoverLift(5).build())
        .build());
```

Публичный API находится в пакете `liquidui.api`.

## Редактор без кода

Мод [Liquid UI Editor](../liquid-ui-editor) позволяет заполнить поля, открыть
предпросмотр и собрать отдельный Fabric JAR прямо в Minecraft. Результат появляется
в `liquid-ui-exports` внутри папки игры. Такой JAR содержит только метаданные и JSON,
а экран строит библиотека при загрузке ресурсов.
