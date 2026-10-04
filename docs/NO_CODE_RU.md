# Liquid UI без написания Java-кода

Начиная с Liquid UI Library 1.2, экран можно описать JSON-файлом. Поместите его в
`src/main/resources/assets/<id_мода>/liquid_ui/extensions/<имя>.json`.

```json
{
  "schema": 1,
  "id": "example_menu:main",
  "title": "Мой экран",
  "menu": {
    "label": "Моё меню",
    "order": 100
  },
  "elements": [
    {
      "type": "button",
      "text": "Настройки",
      "x": -100,
      "y": -28,
      "width": 200,
      "height": 48,
      "action": { "type": "options" }
    },
    {
      "type": "button",
      "text": "Назад",
      "x": -100,
      "y": 32,
      "width": 200,
      "height": 48,
      "action": { "type": "back" }
    }
  ]
}
```

Координаты `x` и `y` считаются от центра экрана. `order` задаёт положение плитки:
чем меньше число, тем раньше она появляется. Кнопкам доступны действия:

- `options` — настройки Minecraft;
- `language` — выбор языка;
- `singleplayer` — выбор мира;
- `multiplayer` — список серверов;
- `main_menu` — главное меню;
- `back` — предыдущий экран;
- `close` — закрыть интерфейс;
- `open_screen` — открыть другой JSON-экран; его ID указывается в `action.target`.

Для полностью визуальной работы установите Liquid UI Editor. Он создаёт готовый
data-only JAR, поэтому пользователю расширения не нужны Java, Gradle или JDK.
