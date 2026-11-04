<!-- 83590 - Valentyna Rusinova -->
# Quality gate Checkstyle – convenção de nomes das constantes (83590)

## Ficheiros

| Ficheiro | Localização |
|---|---|
| Workflow | `.github/workflows/checkstyle-83590.yaml` |
| Configuração do Checkstyle | `Checkstyle/checkstyle-83590.xml` |

## Convenção escolhida

Tipo de identificador: **constantes** (campos `static final`). Regra do Checkstyle: `ConstantName`, com o padrão `^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$` (MAIÚSCULAS separadas por sublinhado, por exemplo `MAX_SHIPS`). O ficheiro de configuração só tem esta regra, com `severity=error`.

Action usada: `dbelyaev/action-checkstyle@v4` (reporter `github-pr-check`, `level: error`, `fail_level: error`, `filter_mode: nofilter`). O workflow corre em *pull request* e falha quando existe alguma violação. A action usou o Checkstyle 14.3.0. Antes do GitHub, a configuração foi testada localmente com o `maven-checkstyle-plugin` 3.6.0 (`mvn checkstyle:check`), sem alterar o `pom.xml`.

## Execuções (violação provocada e corrigida)

| Execução | Resultado | Ligação |
|---|---|---|
| Com a violação (PR #14) | Gate **falha** | https://github.com/LEI-128791/Battleship2/actions/runs/41137004775 |
| Depois de corrigir (PR #14) | Gate **passa** | https://github.com/LEI-128791/Battleship2/actions/runs/41138211346 |

Pull request: https://github.com/LEI-128791/Battleship2/pull/14 (ramo `teste-checkstyle-83590`, fechado sem merge).

Violação provocada: `Battleship2-main/src/main/java/battleship/ConstantNameDemo.java`, linha 9, constante `maxShips`. A anotação do check *reviewdog [checkstyle] report* (separador *Checks* do pull request) indica o ficheiro, a linha e a mensagem: `Name 'maxShips' must match pattern '^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$'.` Correção: a constante foi renomeada para `MAX_SHIPS`. As violações aparecem como anotações do check e não no texto do log do job.

## Proteção do ramo `main` (passo 5)

O `main` tem uma regra de proteção que exige que os checks seguintes passem antes do merge de um pull request: `qodana-83590`, `checkstyle-83590`, `qodana-128782` e `checkstyle-128782`. A regra aplica-se a quem não é administrador do repositório.

Confirmado no PR #19 (https://github.com/LEI-128791/Battleship2/pull/19, ramo `teste-gates-83590`, fechado sem merge), que continha as duas violações ao mesmo tempo:

| Momento | `qodana-83590` | `checkstyle-83590` | Botão de merge |
|---|---|---|---|
| 1. Método longo (81 instruções) e `maxShips` | falha | falha | **bloqueado** |
| 2. Só o método longo corrigido | passa | falha | **bloqueado** (só pelo Checkstyle) |
| 3. Método e constante corrigidos | passa | passa | **desbloqueado** |

Execuções do PR #19:
- Momento 1: Qodana https://github.com/LEI-128791/Battleship2/actions/runs/41740509278 · Checkstyle https://github.com/LEI-128791/Battleship2/actions/runs/41740509254
- Momento 2: Qodana https://github.com/LEI-128791/Battleship2/actions/runs/41741891675 · Checkstyle https://github.com/LEI-128791/Battleship2/actions/runs/41741891796
- Momento 3: Qodana https://github.com/LEI-128791/Battleship2/actions/runs/41742497889 · Checkstyle https://github.com/LEI-128791/Battleship2/actions/runs/41742497963

Nota: os relatórios `checkstyle` (reviewdog) e `Qodana for JVM`, criados pelas próprias ferramentas, têm o mesmo nome em todos os workflows do repositório, por isso o GitHub mostra-os agrupados com um dos workflows; não são checks obrigatórios. Neste trabalho contam os `qodana-83590` e `checkstyle-83590`.

## O que o Checkstyle não consegue verificar

O Checkstyle só valida a **forma** do nome contra o padrão, não se o nome é significativo: `MAX_SHIPS` e `X1` cumprem os dois a regra, mas só o primeiro diz o que a constante representa. A regra `ConstantName` também só se aplica a campos `static final`, por isso não sabe se uma constante devia mesmo existir, nem se um `static final` com nome em maiúsculas guarda um objeto modificável (uma lista, por exemplo) e não um valor constante. Verifica ainda apenas o código Java, e não a documentação nem a utilização correta do nome.
