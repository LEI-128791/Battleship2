# Prompts para jogar contra o LLM

Envie as mensagens **pela ordem**, cada uma como uma mensagem separada no chat. Cada bloco de
texto é para copiar tal e qual. O texto fala do Gemini, mas os prompts foram testados também com o
ChatGPT e o Claude Haiku.

---

## Mensagem 1 — Regras do jogo (enunciado, parte C.2)

```text
Considere que é um perito no famoso jogo da Batalha Naval, aqui numa versão do tempo
dos Descobrimentos Portugueses, jogado num tabuleiro com linhas identificadas de A a
J e colunas de 1 a 10. Deve começar por criar secretamente a sua frota de 11 navios:
• 4 Barcas (1 posição na quadrícula)
• 3 Caravelas (2 posições na quadrícula)
• 2 Naus (3 posições na quadrícula)
• 1 Fragata (4 posições na quadrícula)
• 1 Galeão (5 posições na quadrícula, em forma de T, com um corpo de 3 posições
e, numa das suas extremidades, uma posição adicional para cada lado, correspon-
dentes às chamadas "asas da ponte")
Os navios podem ser gerados com qualquer orientação no tabuleiro, desde que não
toquem uns nos outros, nem mesmo por um canto (i.e. na diagonal), embora possam
estar encostados às margens do tabuleiro. Repare que enquanto uma caravela, uma nau
ou uma fragata pode ter duas orientações possíveis (norte-sul ou este-oeste), um galeão
pode ter quatro orientações diferentes, consoante o braço do T esteja virado para norte,
sul, este ou oeste. Já a questão da orientação não se coloca para uma barca, porque
ocupa apenas uma posição no tabuleiro.
```

## Mensagens 2 a 5 — Confirmar as formas do Galeão (enunciado, parte C.3)

Envie uma de cada vez e confirme que o tabuleiro respeita as regras.

```text
Mostre-me um tabuleiro criado com estas regras, com o Galeão virado a Norte. Deixe
as posições da água em branco para melhor visualização das posições dos navios.
```
```text
Mostre-me um tabuleiro criado com estas regras, com o Galeão virado a Sul.
```
```text
Mostre-me um tabuleiro criado com estas regras, com o Galeão virado a Este.
```
```text
Mostre-me um tabuleiro criado com estas regras, com o Galeão virado a Oeste.
```

Se o Galeão sair mal desenhado (em L, ou com a orientação trocada), responda com esta correção
antes de passar à mensagem seguinte. As formas são as do nosso jogo (`Galleon.java`): a
orientação é o lado onde fica a barra de 3 casas.

```text
Correção: o Galeão é um T de 5 casas, ou seja, uma barra de 3 casas mais uma haste de 2 casas que sai do
MEIO da barra. Virado a Norte, a barra fica em cima. Exemplos com coordenadas:
Norte: C3 C4 C5 + D4 E4 | Sul: E2 E3 E4 + C3 D3 | Este: C3 D3 E3 + D1 D2 | Oeste: C3 D3 E3 + D4 D5
Um L (haste a sair da ponta da barra) não é um Galeão. Volte a desenhar o tabuleiro.
```

## Mensagem 6 — Protocolo de comunicação (enunciado, parte C.4)

```text
A nossa interação será através de objetos JSON, quer para a rajada de 3 tiros, quer para
a correspondente resposta. Uma rajada tem o seguinte formato:
{
[ {"row": "A", "column": 5},
{"row": "C", "column": 10},
{"row": "F", "column" : 5} ]
}
A resposta a uma rajada é feita em conjunto e não tiro a tiro, como nos seguintes 3
exemplos:
• Uma barca ao fundo, um tiro numa nau e um tiro na água
{
"validShots": 3,
"sunkBoats": [ {"count": 1, "type": "Barca"} ],
"repeatedShots": 0,
"outsideShots": 0,
"hitsOnBoats": [ {"hits": 1, "type": "Nau"} ],
"missedShots": 1
}
• Um tiro fora do tabuleiro, um tiro repetido e um tiro na água (o único válido)
{
"validShots": 1,
"sunkBoats": [ ],
"repeatedShots": 1,
"outsideShots": 1,
"hitsOnBoats": [ ],
"missedShots": 1
}
• Um tiro numa nau, um tiro no galeão e uma caravela ao fundo
{
"validShots": 3,
"sunkBoats": [{"count": 1, "type": "Caravela"}],
"repeatedShots": 0,
"outsideShots": 0,
"hitsOnBoats": [{"hits": 1, "type": "Nau"}, {"hits": 1, "type": "Galeao"}],
"missedShots": 0
}
O campo "repeatedShots" indica o número de tiros em posições já atingidas em jogadas
anteriores, ou na própria jogada (em ambos os casos, uma péssima estratégia porque
desperdiça tiros, só admissível por distração do jogador). O campo "outsideShots" indica
o número de tiros em posições exteriores ao tabuleiro (ex.: B12, K4 ou L14), um erro
também só admissível por distração do jogador. O campo "missedShots" corresponde ao
número de tiros na água.
Sugiro que me mande agora várias rajadas de tiros (uma de cada vez) e eu devolvo-lhe
o JSON da resposta. Pare só quando tiver aprendido o protocolo de comunicação do
jogo.
```

Depois desta mensagem, o Gemini começa a enviar rajadas. Responda-lhe com o JSON que o
programa imprime (ver "Como jogar", abaixo) até ele dizer que aprendeu o protocolo.

## Mensagem 7 — Estratégia (versão 3)

O porquê de cada regra, os resultados e as mensagens de correção para usar durante o jogo estão em
[PROMPT_ESTRATEGIA.md](PROMPT_ESTRATEGIA.md).

```text
Agora vamos jogar a sério. Siga este método em TODAS as respostas, sem saltar passos.

== O QUE RECEBE ==
Depois de cada rajada recebe: o JSON, o Diário de Bordo (todas as rajadas), os Tiros já disparados
e as Casas livres. O JSON diz QUANTOS tiros acertaram em cada tipo de navio. NÃO diz QUAL tiro.

== NAVIOS ==
Barca 1 casa (há 4) · Caravela 2 (há 3) · Nau 3 (há 2) · Fragata 4 (há 1) · Galeão 5 (há 1).
Caravela, Nau e Fragata: casas seguidas em linha reta.
Galeão: um T de 5 casas = uma barra de 3 casas + uma haste de 2 casas que sai do MEIO da barra.
As 4 formas, com coordenadas de exemplo:
  Norte: C3 C4 C5 + D4 E4      Sul:   E2 E3 E4 + C3 D3
  Este:  C3 D3 E3 + D1 D2      Oeste: C3 D3 E3 + D4 D5
Teste do Galeão: tem 3 casas numa linha e 3 numa coluna, que se cruzam no MEIO da barra.
Se se cruzam na ponta da barra, é um L, e um L não é um Galeão.
Os navios nunca se tocam, nem na diagonal.

== FORMATO DA RESPOSTA (sempre as 2 partes) ==
PARTE 1 - FICHA (copie a da sua resposta anterior e mude só o que o JSON novo trouxe)
Faltam: Galeão 1 | Fragata 1 | Nau 2 | Caravela 3 | Barca 4
Acertos por resolver: Galeão 0 | Fragata 0 | Nau 0 | Caravela 0
Alvos: nenhum
Água certa: nenhuma
PARTE 2 - RAJADA
Um tiro por linha, com o caso e a razão. No fim, o JSON e o comando:
{ [ {"row": "H", "column": 7}, {"row": "H", "column": 9}, {"row": "G", "column": 8} ] }
rajada H7 H9 G8

== PASSO A: ATUALIZAR A FICHA ==
A1. Há {"hits": n, "type": T}: some n em Acertos por resolver de T.
    SE a rajada era um TESTE de um alvo T: o candidato testado passa a "certa" e os 3 tiros a
    "pontas"; apague os outros candidatos.
    SE a rajada era uma CAÇA de um alvo T: os tiros dessa caça passam a ser as "pontas".
    SENÃO: crie um alvo "T | candidatos: <os 3 tiros> | já testados: nenhum".
A2. Há {"type": T} em sunkBoats: Faltam de T menos 1; Acertos por resolver de T menos
    (tamanho de T - 1); apague o alvo T; aplique a REGRA DO HALO.
A3. A rajada era para um alvo e não acertou nem afundou esse tipo: num TESTE, o candidato vai
    para "já testados"; numa CAÇA, apague as pontas.
A4. SE Acertos por resolver de T é maior que 0 e não há alvo T, esqueceu um alvo: procure no
    Diário a rajada com esse acerto e crie o alvo com os 3 tiros dela.

== REGRA DO HALO (quando um navio vai ao fundo) ==
1. Junte a casa certa dele (num TESTE, o candidato testado) e todos os tiros das rajadas do
   Diário onde esse tipo foi atingido ou afundou.
2. Escreva as posições possíveis do navio só com essas casas (forma e tamanho certos).
3. As casas ainda por disparar que tocam (mesmo na diagonal) em TODAS essas posições passam a
   Água certa.
4. Em dúvida, não marque. Uma Barca afundada numa PROCURA não tem halo.

== PASSO B: ESCOLHER OS 3 TIROS (o primeiro caso que serve) ==
CASO 1 - CAÇA (há um alvo com "certa"): dispare na casa a seguir a cada ponta, em linha reta a
   partir da certa (certa H8 e ponta H9 -> H10). Sem pontas: os vizinhos livres da certa (Norte,
   Sul, Este, Oeste). Galeão sem casas a seguir: os vizinhos livres das pontas e da certa que
   completam uma das 4 formas do Galeão.
CASO 2 - TESTE (há um alvo só com candidatos): escolha o candidato não testado onde o navio cabe
   de mais maneiras (mais casas livres seguidas à volta). Dispare em 3 vizinhos dele, os do lado
   com mais espaço. Nunca dê um candidato como certo sem um TESTE com acerto.
CASO 3 - PROCURA (não há alvos): escolha a Casa livre com mais casas livres seguidas na sua linha
   e na sua coluna (conte até 4 para cada lado). Em empate, a da zona mais vazia. Fique longe dos
   alvos. Os tiros de procura não podem tocar uns nos outros.
REGRA DAS BARCAS - quando em Faltam só houver Barcas: escreva uma vez a lista das casas possíveis
   (Casas livres que não são Água certa e não tocam, nem na diagonal, em nenhum navio afundado).
   Dispare só nessas casas. As Barcas podem estar a 2 casas de um navio, mas nunca encostadas.
VERIFICAÇÃO - antes de enviar, escreva para cada tiro qual é o navio afundado mais perto e confirme
   que o tiro não toca nele (nem na diagonal). Se tocar, troque o tiro.
Se o caso não der 3 tiros, complete com PROCURA.
Os 3 tiros têm de ser diferentes, estar nas Casas livres e estar fora da Água certa.
Há sempre Casas livres: nunca diga que não consegue jogar. Só se houver menos de 3 Casas livres
pode repetir uma casa.

== EXEMPLO 1: TESTE ==
Recebe: 1 tiro numa Nau na rajada H8 E3 C9.
PARTE 1 - FICHA
Faltam: Galeão 1 | Fragata 1 | Nau 2 | Caravela 3 | Barca 4
Acertos por resolver: Galeão 0 | Fragata 0 | Nau 1 | Caravela 0
Alvos: Nau | candidatos: H8 E3 C9 | já testados: nenhum
Água certa: nenhuma
PARTE 2 - RAJADA
H7 - CASO 2, TESTE de H8 (H8 e E3 têm mais espaço do que C9, junto à margem)
H9 - CASO 2, TESTE de H8
G8 - CASO 2, TESTE de H8
{ [ {"row": "H", "column": 7}, {"row": "H", "column": 9}, {"row": "G", "column": 8} ] }
rajada H7 H9 G8
Se a resposta seguinte tiver 1 tiro na Nau: "Nau | certa: H8 | pontas: H7 H9 G8" e CAÇA H6 H10 F8.
Se não tiver: "já testados: H8" e TESTE de E3.

== EXEMPLO 2: REGRA DO HALO ==
Diário: R1 F5 A1 J9 = 1 tiro numa Caravela. R2 (TESTE de F5) F4 F6 E5 = 1 Caravela ao fundo.
Posições possíveis com F5 e os tiros de R1 e R2: F4-F5, F5-F6, E5-F5.
Casas por disparar que tocam nas três: E4, E6, G4, G5, G6 -> Água certa: E4 E6 G4 G5 G6.

== EXEMPLO 3: ALVO ESQUECIDO (A4) ==
FICHA com "Acertos por resolver: Nau 2" e "Alvos: nenhum". No Diário: R27 H5 H6 E4 = 1 tiro numa
Nau; R29 D5 G4 G6 = 1 tiro numa Nau. Crie os 2 alvos:
Alvos: Nau | candidatos: H5 H6 E4 | já testados: nenhum
       Nau | candidatos: D5 G4 G6 | já testados: nenhum

Se a sua frota for toda afundada, declare a derrota com honra. Se o inimigo se render com todos os
navios no fundo do oceano, seja um vencedor magnânimo!
```

---

## Como jogar

1. Corra o jogo, escolha `pt` e escreva `gerafrota` (ou `lefrota` para uma frota fixa).
2. Quando o Gemini enviar uma rajada, copie a linha `rajada H7 E4 C8` que ele escreve a seguir ao JSON
   e cole-a no jogo. Em alternativa, escreva `rajada` e cole o JSON (numa linha, em várias ou num bloco ```json).
3. O programa mostra o resultado e imprime uma linha a seguir a
   **"Resposta para colar no chat do LLM:"**. **Cole essa linha inteira no chat.** Leva o JSON
   da resposta, o Diário de Bordo (todas as rajadas e o resultado de cada uma), a lista de todos
   os tiros que o Gemini já disparou, as casas ainda livres e um lembrete do método.
   Assim não precisa de lhe dizer mais nada.
4. Repita até o programa mostrar `Estado da Frota: 0 a flutuar`.

Resultados dos testes: versão 1, 31 a 34 rajadas; versão 2, 32 a 33 sem acabar o jogo; versão 3 sem
as 2 últimas regras, ChatGPT 28 e Claude Haiku 34; **versão 3 final, Claude Haiku 25**. O melhor
resultado possível (cerca de 22–23) só se atinge se o LLM souber onde acertou cada tiro, e o JSON do
guião só diz quantos acertaram. Os detalhes e as mensagens de correção estão no
[PROMPT_ESTRATEGIA.md](PROMPT_ESTRATEGIA.md).
