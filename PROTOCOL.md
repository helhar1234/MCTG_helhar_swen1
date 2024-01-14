# Protokoll für SWEN Monster Trading Cards Game

## 1) Technische Schritte

### Design
- Beim Design habe ich mich an die API-Spezifikationen und das Curl-File gehalten, da ich zu Beginn keinen klaren Überblick hatte.
- Ich habe Controller, Services und Repositories für die verschiedenen Ebenen erstellt. Im Controller wird nur die erwartete Handhabung durchgeführt, während im Service die Logik ausgeführt wird und über die Repositories mit der Datenbank kommuniziert wird.
- Siehe mein Datenbank-Schema in src/uml/database-schema.puml sowie weitere UML-Diagramme im src/uml/ Ordner.
- Fehlerfälle werden mit Ausnahmen behandelt, die in MtcgApp abgefangen werden, damit sie von überall ausgelöst werden können.

### Herausforderungen
- Am Anfang hatte ich Schwierigkeiten, das gesamte System zu verstehen und mich einzuarbeiten, was zu Verzögerungen beim Design führte.
- Ich habe anfangs das Token-basierte Authentifizierungskonzept falsch verstanden, was zu Problemen mit dem Curl-File führte. Die Session-Route wurde neu implementiert und funktionierte dann einwandfrei. Anfangs habe ich nicht verstanden, dass der Token im Header mitgesendet wird, weswegen ich beim login ein User Objekt erstellt habe, dass nach 20 min zerstört wird, dies führte jedoch zu Fehlern beim curl Script)
- Gelegentliche Schwierigkeiten mit Git, insbesondere beim Erstellen und Wechseln zwischen Branches. Hier habe ich anfangs bei der Erstellung des Repos Probleme gehabt, da sich der master Branch (von IntelliJ) und dann dr main Branch (vom Repo) nicht gut verstanden haben. Weiters hatte ich ab und zu das Problem, dass ich gewisse Files in einem anderen Branch committen wollte als im aktuellen. Hierfür habe ich "git stash" und "git stash pop" verwendet. Hierbei sind hin und wieder Files verloren gegangen. Den genauen Grund konnte ich nicht herausfinden, jedoch habe ich einfach angefangen besser zu kontrollieren, ob alle Files im stash sind.
- Zeitprobleme in der Datenbank: Beim speichern in meiner DB hatte ich immer 1h Zeitunterschied. Dies aber nur, wenn es durch die MtcgApp oder in der IntelliJ console ausgeführt wurde. Dann dachte ich, dass es an der Datenbank liegt, dass hier evt. die falsche Zeitzone eingestellt ist. Mit pgAdmin habe ich dies ausprobiert und versucht CURRENT_TIMESTAMP einzufügen. Hier ist jedoch die richtige Zeit rausgekommen und in den Einstellungen habe ich sonst auch nichts gefunden, was auf eine falsche Zeitzone hindeuten könnte. Dann dachte ich, dass der Docker-Container evt. in einer falschen Zeitzone läuft. Ich habe einige commands ausprobiert. Diese haben jedoch nichts geändert. Dann dachte ich, dass es ja nurmehr an IntelliJ liegen kann. Hier habe ich jedoch in den Einstellungen nichts gefunden. Dann habe ich nochmal die DB genauer angesehen und bin auf das Problem gewstoßen. Bei "SHOW TIMEZONE" war "UTC" eingestellt.
- An einem Tag hatte ich Schwierigkeiten mit Hoppscotch, da es meine Requests immer 2x an den Server gesendet hat, was zu Problemen beim UNIQUE Contraint kam. Ich habe es auch mit der TaskApp ausprobiert und hier war das gleiche. Bei einer Kollegin ebenfalls. Ich bin zwar nie auf eine Lösung gekommen, jedoch hatte Hoppscotch am nöchsten Tag das Problem nicht mehr.

### Ausgewählte Lösungen
- Token-basiertes Login mit Sessions, die in die DB gespeichert werden und nach 20 Minuten nicht mehr gültig sind.
- Aufmerksameres Handling von Git beim Commiten und Wechseln zwischen Branches, damit keine Files mehr verloren gehen.
- Behebung der Zeitzone-Probleme in der Datenbank durch SET timezone='CET'.

## 2) Unit Tests
- Ich persönlich bin kein großer Fan vom Testing, aber ich habe trotzdem einige Unit-Tests für mein Projekt geschrieben.
- Für mich sind Unit-Tests ein wichtiger Bestandteil der Qualitätssicherung, da sie dazu beitragen, Probleme und Fehler im Code frühzeitig zu identifizieren, bevor sie in die Produktionsumgebung gelangen.
- Meine Unit-Tests haben sich auf verschiedene Teile meines Codes konzentriert: Services und Repositorys.
- Einige Beispiele für typische Testfälle, die ich geschrieben habe, sind:
    - Überprüfung, ob ein Benutzer erfolgreich erstellt und in der Datenbank gespeichert wurde.
    - Testen, ob das Erstellen eines Trades unter gültigen Bedingungen erfolgreich ist.
    - Überprüfen, ob bei einem Battle auch ein Unentschieden möglich ist.
    - Testen, ob die Erstellung von Transaktionen erfolgreich ist, wenn ausreichend Münzen vorhanden sind.
    - Überprüfen, ob die Wheel of Fortune-Funktion Preise korrekt vergibt.
    - und viele weitere

## 3) Zeitaufzeichnung
- Ich habe im November und Dezember aufgrund anderer LVs kaum am Projekt gearbeitet.
- In den Weihnachtsferien habe ich etwa 1-2 Woche lang täglich 4-7 Stunden daran gearbeitet.
- Nach den Ferien habe ich im Durchschnitt täglich 1-3 Stunden am Abend daran gearbeitet, da ich abends am besten programmieren kann.
