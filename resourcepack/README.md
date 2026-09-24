# LainaPlots icons

Każda ikona jest osobnym plikiem PNG w `assets/lainaplots/textures/item/`.

Mapowanie domyślnych identyfikatorów:

- `owner_plot.png` -> `71001`
- `member_plot.png` -> `71002`
- `favorite_owner_plot.png` -> `71003`
- `favorite_member_plot.png` -> `71004`
- `summary.png` -> `71005`

Podmień wybrany PNG, zachowując nazwę i przezroczystość, a następnie uruchom:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\build-resource-pack.ps1
```

Skrypt tworzy `src/main/resources/LainaPlots-Icons.zip`, który Maven pakuje do JAR-a.
Na działającym serwerze można też bezpośrednio podmienić
`plugins/LainaPlots/LainaPlots-Icons.zip` i wykonać `/dzialki reload`.
