# Refactoring Opportunities

| Local | Cheiro no código | Refabricação | Nº aluno |
|---|---|---|---|
| `Tasks::readClassicPosition` | Dead Code | Safe Delete | 83590 |
| `Ship::isInsideBoard` | Dead Code | Safe Delete | 83590 |
| `Tasks::menu` | Long Method | Extract Method (`exportarHistorico`) | 83590 |
| `Tasks::menu` | Magic Literal | Extract Constant | 83590 |
| `Tasks::exportarHistorico` | Magic Literal | Introduce Parameter | 83590 |
| `Tasks::menu` | Unnecessary Temporary Variable | Inline Variable | 83590 |
| `Ship::occupies`, `tooCloseTo`, `shoot`, construtor | Mysterious Name | Rename (parâmetro `position`) | 83590 |
| `Ship::isInsideBoard` | Duplicated Code | Introduce Variable (`lastIndex`) | 83590 |
| `Ship::size` | Unnecessary Boxing | Type Migration (`Integer` → `int`) | 83590 |
| `Position::adjacentPositions` | Magic Literal | Extract Constant (`DIRECTIONS`) | 83590 |
| `IPosition::equals` | Redundant Method Override | Safe Delete | 83590 |
| `Tasks` | Large Class | Extract Class (`MenuCommands`) | 83590 |
| `Game::printBoardFrame` | Duplicated Code | Extract Method (`printBorder`) | 128791 |
| `Game::randomEnemyFire` | Long Method | Extract Method (`buildUsablePositions`) | 128791 |
| `Game::countInvalidShots`, `countRepeatedShots`, `countHits`, `countSinks` | Unnecessary Boxing | Type Migration (`Integer` → `int`) | 128791 |
| `Game::over` | Duplicated Code | Extract Constant | 128791 |
| `Game::printShips` | Mysterious Name | Rename (`shipPos`, `adjacentPos`) | 128791 |
| `Game::getRemainingShips` | Unnecessary Temporary Variable | Inline Variable | 128791 |
| `Game::fireSingleShot` | Duplicated Code | Introduce Variable (`sunk`) | 128791 |
| `Game::fireShots` | Dead Code | Safe Delete | 128791 |
| `Game` | Large Class | Extract Class (`BoardPrinter`) | 128791 |
| `Move::processEnemyFire` | Long Method | Extract Method (`countShots`, `buildMessage`, `buildJson`) | 128791 |
| `Move::processEnemyFire` | Duplicated Code | Extract Method (`buildBoatList`) | 128791 |
| `Move::buildJson`, `buildMessage` | Long Parameter List | Change Signature | 128791 |
| `PdfReportExporter::exportHistory` | Long Method | Extract Method (`buildLines`, `writePage`) | 128782 |
| `PdfReportExporter` | Magic Literal | Extract Constant | 128782 |
| `PdfReportExporter::export` | Duplicated Code | Extract Method (`coordinateOf`) | 128782 |
| `PdfReportExporter::describeResult` | Duplicated Code | Introduce Variable (`category`) | 128782 |
| `Frigate`, `Carrack`, `Caravel` (construtores) | Duplicated Code | Pull Members Up (para `Ship`) | 128782 |
| `Carrack` | Speculative Generality | Optimize Imports | 128782 |
| `Main::main` | Magic Literal | Extract Constant | 128782 |
| `Galleon::fillSouth`, `fillEast` | Mysterious Name | Extract Variable (só dar nome, sem alterar a conta) | 128782 |
| `Caravel` (construtor) | Duplicated Code | Extract Method (`fillVertical`, `fillHorizontal`) | 128782 |
| `Compass::randomBearing` | Unnecessary Temporary Variable | Inline Variable | 128782 |
| `Fleet::colisionRisk` | Mysterious Name | Rename (`collisionRisk`) | 105447 |
| `Fleet::printShips` | Mysterious Name | Rename (parâmetro) | 105447 |
| `Fleet::createRandom` | Magic Literal | Extract Constant (`SHIP_TYPES`) | 105447 |
| `Fleet::createRandom` | Dead Code | Safe Delete | 105447 |
| `Fleet::getShipsLike`, `getFloatingShips`, `getSunkShips` | Duplicated Code | Extract Method (`filterShips`) | 105447 |
| `Fleet::addShip` | Complex Condition | Introduce Variable (`canAdd`) | 105447 |
| `Fleet::printStatus` | Dead Code | Safe Delete | 105447 |
| `IMove::readMove` | Inappropriate Intimacy | Move Static Members (para `Move`) | 105447 |
| `IFleet::FLEET_SIZE` | Unnecessary Boxing | Type Migration (`Integer` → `int`) | 105447 |
| `Compass::c` | Mysterious Name | Rename Field (`direction`) | 105447 |
