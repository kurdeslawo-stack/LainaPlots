# LainaPlots

LainaPlots jest lekkim GUI dla istniejących domów ProtectionStones na Paper 26.2. Plugin nie tworzy regionów i nie wykonuje teleportacji samodzielnie.

## Wymagania

- Paper 26.2
- ProtectionStones 2.10.2
- Java 25

## Komendy i uprawnienia

- `/dzialki` oraz alias `/plots` — otwarcie GUI; `lainaplots.use` (domyślnie każdy gracz).
- `/dzialki reload` — przeładowanie konfiguracji, w tym Material i CustomModelData ikon; `lainaplots.reload` (domyślnie operator).
- Teleport wymaga uprawnień sprawdzanych przez ProtectionStones, przede wszystkim `protectionstones.home`. LainaPlots ich nie duplikuje.

## Teleportacja

Kliknięcie działki zamyka GUI i wykonuje jako gracz komendę skonfigurowaną w `base_command` ProtectionStones:

```text
ps home <identyfikator>
```

Dla działki w aktualnym świecie identyfikatorem jest dokładny region ID. Dzięki temu `ArgHome` i `ArgTp` ProtectionStones odpowiadają za uprawnienia, dostęp memberów, `prevent_ps_home`, opóźnienie, anulowanie po ruchu, bypass oraz komunikaty.

ProtectionStones 2.10.2 wyszukuje region ID tylko w aktualnym świecie, natomiast nazwy regionów są globalne. Dlatego działka z innego świata jest delegowana przez nazwę ustawioną w ProtectionStones. Przed wykonaniem komendy LainaPlots sprawdza publiczną metodą wyszukiwania PS, czy nazwa rzeczywiście obejmuje kliknięty region; chroni to przed kolizją z lokalnym ID. Jeśli odległa działka nie ma nazwy albo nazwa nie rozwiązuje się do tego regionu, LainaPlots nie wykonuje obejścia ani własnego teleportu i pokazuje bezpieczny komunikat. Nazwę można ustawić przez `/ps name`.

## Ikony GUI

Każda ikona obsługuje `material` oraz prostą dodatnią wartość `custom-model-data`. W Paper 26.2 wartość jest zapisywana przez `CustomModelDataComponent` jako pierwszy element listy `floats`. Wartość `0`, liczba ujemna albo brak pola oznacza brak CMD.

| Klucz | Domyślny Material |
|---|---|
| `filter` | `HOPPER` |
| `summary` | `BOOK` |
| `previous-page` | `ARROW` |
| `next-page` | `ARROW` |
| `refresh` | `SUNFLOWER` |
| `filler` | `BLACK_STAINED_GLASS_PANE` |
| `owner-plot` | `GRASS_BLOCK` |
| `member-plot` | `CYAN_STAINED_GLASS` |
| `loading` | `CLOCK` |
| `empty-state` | `FLOWER_POT` |

Przykład:

```yaml
gui:
  icons:
    owner-plot:
      material: GRASS_BLOCK
      custom-model-data: 1001
    member-plot:
      material: CYAN_STAINED_GLASS
      custom-model-data: 1002
```

Nieprawidłowy, powietrzny lub niebędący itemem Material powoduje czytelny warning w logu i użycie domyślnego Materialu danej ikony. Starszy `config.yml` bez `gui.icons` działa bez migracji i używa wszystkich fallbacków.

## Budowanie

```text
mvn clean test
mvn package
```

Wynik: `target/LainaPlots-0.1.0.jar`.
