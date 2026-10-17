# Prompt de estratégia (Sprint parte 2) — versão 3

Este é o prompt que se envia **depois** das mensagens 1 a 6 do [PROMPTS_GEMINI.md](PROMPTS_GEMINI.md)
(regras, formas do Galeão e protocolo JSON). Copie o bloco abaixo tal e qual, numa só mensagem.

A versão 3 foi escrita a pensar em modelos fracos: em cada jogada, o LLM copia uma FICHA curta e
segue passos numerados. É o LLM que conta e deduz, como pede o guião. O programa não faz deduções e
não mexe no JSON; só junta, à linha para colar, o Diário de Bordo, os tiros já disparados e as
casas livres. Tudo isto é informação que o LLM já recebeu.

## Resultados dos testes

| Versão | Jogos reais | Problema principal |
|---|---|---|
| v1 (mapa 10x10 + verificação) | ChatGPT 33 e 34, Gemini 31 rajadas | Esquecia resultados, o mapa estragava-se, varria o tabuleiro |
| v2 (Diário + 4 listas + CAÇA/TESTE/PROCURA) | Gemini 33+ (ficou 1 Nau por afundar), ChatGPT 32+ (desistiu) | Formato cumprido, mas: alvos esquecidos, candidatos dados como certos sem teste, Galeão em 8 rajadas, nenhum halo |
| v3 sem as 2 últimas regras | ChatGPT 28, Claude Haiku 34 rajadas | Tiros encostados a navios já afundados (27 no Haiku, 14 no ChatGPT) e 9 rajadas para as 2 últimas Barcas |
| **v3 final (+ REGRA DAS BARCAS + VERIFICAÇÃO)** | **Claude Haiku 25 rajadas** | — |

## O que o simulador mostrou

300 frotas aleatórias, com respostas agregadas como no jogo (não se sabe qual tiro acertou) e
regras seguidas à letra:

| Versão das regras | Média | Jogos com ≤ 26 |
|---|---|---|
| Halo só à volta da casa certa | 28,4 | 15% |
| + halo seguro (posições possíveis com os tiros do Diário) | 27,1 | 32% |
| + TESTE do candidato mais provável | 26,9 | 37% |
| **+ PROCURA "linha + coluna livres" (= v3)** | **26,6** | **41%** |
| Limite: saber sempre onde estavam os navios afundados | 22,7 | — |

- **Limite:** o melhor resultado (cerca de 22–23 rajadas) só é possível se o LLM souber onde
  acertou cada tiro. É isso que diz onde estavam os navios afundados e, com eles, toda a água à
  volta. O JSON do guião só diz quantos tiros acertaram em cada tipo, não quais. Saber só qual tiro
  da procura acertou poupa pouco (0,7 rajadas). O que faz falta é saber as casas de cada navio
  afundado, sobretudo das Barcas apanhadas na procura ("num de 3 tiros"). A REGRA DO HALO recupera
  a parte que se pode deduzir com segurança.
- Uma versão do halo que só usava as casas da caça foi rejeitada, porque chegava a marcar água onde
  havia navio.
- **Expectativa:** um modelo fraco que siga tudo fica perto das 26–27; um modelo real erra mais.
  Para ficar sempre nas 26 ou menos, use um modelo com raciocínio (Gemini em modo de raciocínio,
  ChatGPT com "Pensar"): cerca de 23–25.

---

## Mensagem 7 — Estratégia

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

## Mensagens de correção (few-shot durante o jogo)

Quando o LLM se engana, cole uma destas mensagens **antes** da resposta do programa, na mesma
mensagem. Troque os valores entre `<>`.

```text
Correção: a sua FICHA não é a da resposta anterior. Copie a FICHA anterior e mude só o que o JSON
novo trouxe (PASSO A).
```
```text
Correção: <F10> ainda não foi testado. Um candidato só passa a "certa" depois de um TESTE com
acerto. Faça o TESTE de <F10>.
```
```text
Correção: Acertos por resolver de <Nau> é <2>, mas não tem nenhum alvo <Nau>. Aplique a regra A4.
```
```text
Correção: <F8> não é Água certa. Há uma posição possível de <Galeão> que não toca em <F8>.
Retire-a da Água certa.
```
```text
Correção: <E7> não está nas Casas livres. Troque esse tiro.
```
```text
Correção: <E8> toca na <Caravela E7-F7>, que já afundou. Faça a VERIFICAÇÃO e troque o tiro.
```

## Dicas para o jogo correr bem

- Use um modelo com raciocínio (Gemini em modo de raciocínio, ChatGPT com "Pensar"). Os modelos
  rápidos perdem-se mais.
- Uma conversa nova por jogo.
- Para comparar versões do prompt, jogue com a mesma frota (`lefrota`).
- Ao primeiro erro, cole a mensagem de correção certa. Um erro não corrigido repete-se.
