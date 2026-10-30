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

## Proteção do ramo `main`

Não foi possível configurar a regra de proteção: a conta `LEI-83590` tem permissão de escrita (`push`), mas não de administrador, e as definições do repositório (*Settings > Branches*) só estão disponíveis para o dono, `LEI-128791`. Por isso, para já, o botão de merge do PR #14 **não ficou bloqueado** com o gate vermelho. Esta secção será atualizada quando a regra for ativada (exigir o check `checkstyle`).

## O que o Checkstyle não consegue verificar

O Checkstyle só valida a **forma** do nome contra o padrão, não se o nome é significativo: `MAX_SHIPS` e `X1` cumprem os dois a regra, mas só o primeiro diz o que a constante representa. A regra `ConstantName` também só se aplica a campos `static final`, por isso não sabe se uma constante devia mesmo existir, nem se um `static final` com nome em maiúsculas guarda um objeto modificável (uma lista, por exemplo) e não um valor constante. Verifica ainda apenas o código Java, e não a documentação nem a utilização correta do nome.
