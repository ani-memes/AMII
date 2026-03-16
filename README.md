<div align="center">
    <img height="256px" src="https://amii.assets.unthrottled.io/visuals/mocking/aqua_mocking_laugh.gif" ></img>
</div>

# AMII (Anime Meme IDE Integration - versao de update)

![Build](https://github.com/ani-memes/AMII/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/15865-amii.svg)](https://plugins.jetbrains.com/plugin/15865-amii)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/15865-amii.svg)](https://plugins.jetbrains.com/plugin/15865-amii)

<!-- Plugin description -->
Dê mais personalidade ao seu IDE e tenha <emphasis>mais</emphasis> diversão programando com a **A**nime **M**eme **I**DE
**I**ntegration! (AMII)<br/><br/>
Após a instalação, nossa Unidade de Conhecimento de Inferência de Memes (ou MIKU para abreviar)
começará a interagir com você enquanto você constrói código. MIKU sabe quando seus programas falham em executar ou quando testes passam/Falam. Seu novo
companheiro tem a capacidade de reagir a esses eventos. O que provavelmente tomará a forma de um meme de anime do seu:
waifu, husbando e/ou personagem(s) favorito(s)!<br/><br/>

<!-- Plugin description end -->

## Instalação

- Usando o sistema de plugins integrado do IDE:

  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Anime Memes"</kbd> >
  <kbd>Install Plugin</kbd>

- Manualmente:

  Baixe a [última versão](https://github.com/ani-memes/AMII/releases/latest) e instale-a manualmente usando
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---

# Documentação

- [Recursos](#recursos)
  - [Interações](#interações)
    - [Saudação de Inicialização](#saudação-de-inicialização)
    - [Resultados de Testes](#resultados-de-testes)
    - [Tarefas de Build](#tarefas-de-build)
    - [Esperando](#esperando)
    - [Códigos de Saída](#códigos-de-saída)
    - [Logs](#logs)
    - [Sob Demanda](#sob-demanda)
  - [Personalidade](#personalidade)
    - [Frustração](#frustração)
    - [Arrogante](#arrogante)
    - [Tédio](#tédio)
    - [Decepção](#decepção)
    - [Status](#status)
  - [Modo Mínimo](#modo-mínimo)
  - [Modo Discreto](#modo-discreto)
  - [Info ao Clicar](#info-ao-clicar)
  - [Mostrar Meme Anterior](#mostrar-meme-anterior)
  - [Modo Offline](#modo-offline)
  - [Limpar Memes](#limpar-memes)
  - [Suporte ao Rider](#suporte-ao-rider)
  - [Suporte ao Android Studio](#suporte-ao-android-studio)
  - [Assets Personalizados](#assets-personalizados)
    - [Rotulagem Automática](#rotulagem-automática)
    - [Conteúdo Sugestivo](#conteúdo-sugestivo)
- [Configuração](#configuração)
  - [Som](#som)
  - [Conteúdo](#conteúdo)
  - [Exibição](#exibição)
  - [Sincronização de Assets](#sincronização-de-assets)
    - [Visualização de Assets](#visualização-de-assets)
- [Extras](#extras)
  - [O Tema Doki](#o-tema-doki)
  - [Waifu Motivator](#waifu-motivator)
  - [Canal de Lançamento](#quer-atualizações-do-amii-mais-cedo)
- [Atribuições](#atribuições)

---

# Recursos

Aqui está uma lista abrangente de todas as funcionalidades atuais que o AMII oferece.

## Interações

Esta é a proverbial carne e batatas do plugin. Você terá os melhores memes de anime entregues diretamente ao seu
IDE enquanto programa à vontade.

**MIKU**

Como mencionado anteriormente, nossa Unidade de Conhecimento de Inferência de Memes (ou MIKU para abreviar)
tem a capacidade de interagir com você enquanto constrói código. O método preferido de comunicação do MIKU é Memes de Anime.

Se você está se perguntando o que pode fazer para que MIKU dê memes a você, bem, olhe abaixo!

_Todos os eventos são configuráveis para serem ativados/desativados, veja [configuração](#configuração) para mais detalhes_

### Saudação de Inicialização

Apenas abrir um projeto no seu IDE é motivo para celebração. Geralmente, MIKU está animado para vê-lo novamente. Fica
escuro quando você vai embora, então fique por aqui um pouco!

![Saudação de Inicialização](./readmeAssets/project_load.gif)

> Nota: Todas as notificações de espera são definidas para a opção de demissão `timed`.
> Veja a seção [demissão](#demissão) para mais detalhes

### Resultados de Testes

Testes passam e testes falham, isso é apenas um fato da vida. Você sabe o que é melhor do que x's vermelhos e marcas verdes?

> Memes de Anime

**Teste Passa**
![Teste Passa](./readmeAssets/test_pass.gif)

**Falhas de Teste**
![Falha de Teste](./readmeAssets/test_fail.gif)

### Tarefas de Build

Este é um IDE, certo? Bem, isso significa que você pode construir código diretamente do seu editor. Acontece que, builds
acontecem de falhar de vez em quando. Você colocou aquele ponto e vírgula, certo?

![Falhas de Build](./readmeAssets/build.gif)

Bem, MIKU sabe quando seus builds falham também, então espere uma resposta também.

**Build Passa**

Quando você recupera todas as suas contas, da próxima vez que construir com sucesso (após uma falha de build) MIKU dará um
tapinha nas costas.

### Esperando

O que você quer dizer que não programa o tempo todo? Você quer dizer que há períodos de tempo quando você não está usando seu IDE?

![Esperando](./readmeAssets/waiting.gif)

Bem, MIKU fica solitário, ou um pouco entediado quando você vai embora.

> Nota:
> - Todas as notificações de espera são definidas para a opção de demissão `focus loss`.
    > Veja a seção [demissão](#demissão) para mais detalhes.
> - As notificações por padrão são definidas para o centro,
    > mas podem ser configuradas nas [configurações](#configuração).

### Códigos de Saída

Então você conseguiu fazer seu código construir e implantar. No entanto, o programa tem um erro catastrófico, que fez seu pobre
aplicação terminar com um código de saída triste.

![Códigos de Saída](./readmeAssets/exit_code.gif)

Como você provavelmente adivinhou agora, MIKU está sempre observando, e tem a capacidade de responder aos seus erros.

#### Reações de Código de Saída Negativo

Realmente, qualquer coisa que sai com um valor não-zero significa que seu programa morreu inesperadamente. Então em vez de fornecer uma lista exaustiva de códigos de saída, MIKU apenas reagirá negativamente a qualquer código que seja:
__Ignorado__ ou __Positivo__.

**Códigos de Saída Ignorados**

Programas que saem com:

- **0**: Saiu sem problema
- **130**: Você terminou o processo (ex. pressionou o botão de parada)

fazem parte dos códigos de saída permitidos padrão, MIKU não reagirá negativamente a esses (mas pode se você quiser).

#### Reações de Código de Saída Positivo

Se você quiser um tapinha nas costas quando seu programa terminar corretamente, seu servo virtual doméstico pode ser configurado para
fazer isso também.

### Quebrador de Silêncio

Então você tem trabalhado diligentemente construindo seu código, mas não usando nenhum recurso do seu IDE. Como construir, testar,
ou executar seu projeto. Bem, MIKU gosta de lembrá-lo de vez em quando que eles existem.

Você pode especificar quanto tempo você pode ir sem ver um meme. Depois disso, MIKU dará um a você!

### Logs

Você trabalha em um projeto que leva um bilhão de anos para a aplicação iniciar? Boas notícias! Seus dias de olhar fixamente para
seus logs acabaram.

![Logs](./readmeAssets/log_watcher.gif)

Você pode pedir gentilmente ao MIKU para assistir os logs por você. Espere uma notificação sempre que sua frase aparecer na sua saída logada!

### Sob Demanda

`Tools | AMII Options | Show random Ani-Meme`

Suponho que se você estiver entediado, ou apenas quiser mostrar seus Memes de Anime, você tem a capacidade de obter memes sob demanda.

![Sob-demanda](./readmeAssets/on_demand.gif)

## Personalidade

Não me entenda mal, ter memes de anime exibidos no meu IDE é incrível, mas você sabe o que é melhor? Ter reações personalizadas
sob medida, quase como se os memes exibidos fossem escolhidos à mão apenas para você!

Acontece que, MIKU tem vários núcleos de personalidade instalados que permitem tal funcionalidade.

### Frustração

MIKU é uma máquina de estado de humor bastante temperamental e tem muitas reações a vários eventos como:

- Esperando por você voltar quando você está longe por algum tempo
- Estando realmente feliz quando seus testes passam.
- Ficando chateado quando seus builds quebram e testes falham.

Graças aos avanços na tecnologia, MIKU agora também tem a capacidade de sentir sua frustração quando **as coisas não estão
funcionando, POR QUE ELES NÃO ESTÃO FUNCIONANDO!!**. MIKU acha bom injetar um pouco de humor na mistura e mostrar a você que
eles estão frustrados também.

**Frustração**
![Frustração](./readmeAssets/frustration.gif)

Como bônus, eles também têm a capacidade de evoluir de estar frustrado para raiva total. Isso só acontece quando
você tem acionado eventos negativos no estado de frustração por um determinado período.

**Raiva**
![Raiva](./readmeAssets/enraged.gif)

Nem toda pessoa quer que seu companheiro fique frustrado. Então você tem a capacidade de desabilitar essa parte da personalidade deles, prevenindo-os de nunca ficarem frustrados em primeiro lugar! 😄

**Tome um Calmante!**

`Tools | AMII Options | Relax MIKU`

Você acidentalmente chateou MIKU? Eu sei que eu tenho (programar é difícil). Bem, felizmente há uma ação `Relax MIKU` que
funciona como descrito. Isso resetará o estado do núcleo de personalidade para que você possa continuar a bagunçar como quiser.

Além disso, a frustração do MIKU também esfriará ao longo do tempo sem a necessidade da sua intervenção.

### Arrogante

Você já esteve em um beco sem saída onde qualquer coisa que você faz acaba falhando? Lembra daquela sensação de finalmente consertar
o problema?

Bem, quando você finalmente coloca todos os seus patos em fila, você e MIKU podem se sentir um pouco arrogantes.

Por exemplo, se seu teste falha em executar, da próxima vez que seus testes passam, você tem uma chance de obter uma reação _arrogante_.

![Smugumin](./readmeAssets/smug.gif)

### Tédio

Quanto mais tempo você fica longe do seu IDE, mais MIKU fica entediado.

Eles começarão esperando pacientemente pelo seu retorno. No entanto, conforme o tempo passa, você verá que eles não podem se entreter para sempre.

Não se surpreenda se você voltar, e eles estiverem dormindo!

### Decepção

Com o poder da tecnologia, você agora tem a capacidade de desapontar seus pais _e_ seu novo assistente virtual!
Estou brincando, sério, MIKU é programado para ter dificuldade em lidar com problemas. Quando todos os seus testes têm sido passando, pode ser um pouco _chocante_ descobrir que seu teste falhou. Mesmo após depurar, e você falha em consertá-lo, MIKU vai se sentir um pouco _decepcionado_ que as coisas não estão funcionando. Você melhor acreditar que se as coisas continuarem a
não funcionar, MIKU vai parar de se sentir mal e apenas ficar _levemente decepcionado_. Cuidado amigo, nós não queremos que isso [evolua para frustração!](#frustração).

![Cadeia de Decepção](./readmeAssets/disappointment_chain.gif)

Apenas você tem o poder de não desapontar seu novo amigo virtual, então trabalhe mais!

### Status

Quer saber como MIKU está se sentindo no momento? Eles têm a capacidade de exibir seu estado emocional atual na
sua barra de status.

![Barra de Status de Humor](./readmeAssets/mood_status_bar.png)

## Modo Mínimo

MIKU pode ser bastante tagarela às vezes, especialmente se você está tentando descobrir como fazer seu teste de integração funcionar. Com `Modo Mínimo` você tem a capacidade de dizer ao MIKU para reagir apenas a eventos que são diferentes. Então quando seus
testes falham várias vezes, você verá apenas uma reação de falha. No entanto, sempre que você quebra seu build, ou seus
testes passam, você obterá uma notificação então.

## Modo Discreto

<img src="https://user-images.githubusercontent.com/15972415/132107791-3d87fa95-3b36-46b1-a49f-ff45a1fb032c.png" alt="See No Problem" align="right" />

Você ainda é um weeb no armário? Você ainda sente vergonha de gostar de anime? Seu trabalho exige que você não tenha diversão?
Em vez de abordar os problemas reais, você pode apenas dizer ao MIKU para fingir ser invisível, com `Modo Discreto`!
Eles entendem, e limparão qualquer conteúdo de anime do IDE, e até esconderão o humor na barra de status. Dessa forma
você não tem que explicar nada para ninguém. Quando a costa estiver clara, apenas desmarque a configuração ou alterne a ação,
e MIKU reaparecerá e retomará seus deveres como seu companheiro virtual.

Este plugin também é integrado com [O Tema Doki](https://github.com/doki-theme/doki-theme-jetbrains#discreet-mode),
para a experiência máxima de esconder vergonha.
Habilitar/desabilitar `Modo Discreto` no Tema Doki habilitará/desabilitará `Modo Discreto` para este plugin.

## Info ao Clicar

Curioso sobre a fonte de uma reação fornecida por MIKU?
Este recurso é habilitado por padrão, e você tem a capacidade de configurá-lo via o menu de configurações, ou mesmo na
notificação de informação.
Apenas clique dentro do meme ativo, e você obterá uma notificação sobre a fonte no canto inferior direito.
Eu tentei marcar tantos assets quanto possível com informações precisas.
No entanto, há alguns assets que eu não sei a fonte, desculpe antecipadamente se você queria saber o anime!

**Nota**: Clicar em um meme, muda o modo de demissão.
[Por favor veja esta documentação para mais informação.](#demissão)

![Info ao clicar](./readmeAssets/info_on_click.gif)

## Mostrar Meme Anterior

Caso você tenha perdido algo, você agora tem a capacidade de dizer ao MIKU, para mostrar sua reação anterior.
Quer você tenha perdido sua chance de [mostrar info ao clicar](#info-ao-clicar) ou você apenas quer ver a reação novamente.
A ação `Mostrar Meme Anterior` é acessível via
<kbd>Tools</kbd> > <kbd>AMII Options</kbd> > <kbd>Show Previous Meme</kbd>

## Modo Offline

Se você se encontrar programando sem qualquer internet, não se preocupe amigo, você pode levar MIKU com você. Todas as interações
que você viu até agora foram armazenadas em um lugar seguro no seu computador, apenas para tal ocasião!

## Limpar Memes

Por qualquer razão, se você tiver um meme despachado que é invulnerável a ir embora, não tenha medo amigo!
Isso é exatamente para o que o `Limpar Memes` foi feito, acessível via
<kbd>Tools</kbd> > <kbd>AMII Options</kbd> > <kbd>Clear Memes</kbd>

## Suporte ao Rider

O IDE Rider é um floco de neve especial que requer amor e atenção extra para fazer o AMII funcionar. Se você instalou o
plugin no Rider, você provavelmente já foi solicitado a instalar
a [Extensão Anime Memes - Rider](https://github.com/ani-memes/amii-rider-extension). Se você perdeu,
por favor [certifique-se de instalar](https://github.com/ani-memes/amii-rider-extension/tree/main#installation), para que você não
perca nenhuma funcionalidade.

<div align="center">
    <img height="256px" src="https://resources.jetbrains.com/storage/products/rider/img/meta/rider_logo_300x300.png" ></img>
</div>

## Suporte ao Android Studio

O Android Studio também é um floco de neve especial que requer amor e atenção extra para fazer o AMII funcionar. Se você
instalou o plugin no Android Studio, você provavelmente já foi solicitado a instalar
a [Extensão Anime Memes - Android](https://github.com/ani-memes/amii-android-extension). Se você perdeu,
por favor [certifique-se de instalar](https://github.com/ani-memes/amii-android-extension/tree/main#installation), para que você não
perca nenhuma funcionalidade.

<div align="center">
    <img height="256px" src="https://1.bp.blogspot.com/-LgTa-xDiknI/X4EflN56boI/AAAAAAAAPuk/24YyKnqiGkwRS9-_9suPKkfsAwO4wHYEgCLcBGAsYHQ/s0/image9.png" ></img>
</div>

## Assets Personalizados

Você tem um conjunto específico de memes que gostaria que MIKU pudesse usar?
Boas notícias! Você pode usar o recurso `Assets Personalizados` para adicionar memes ao seu coração's content.

Diabos, os memes nem precisam ser relacionados a anime!

### Usando Assets Personalizados

Há um conjunto específico de requisitos para que MIKU possa usar seu conteúdo personalizado.

- O asset deve estar em algum lugar no seu `Diretório de Assets Personalizados` definido (MIKU pesquisa recursivamente).
- A imagem deve ser um `GIF`.
- O asset deve ser marcado com pelo menos uma categoria (assets podem ser marcados com mais de uma categoria).

#### Categorias de Meme

Veja os detalhes de resumo abaixo para ver exemplos de assets associados a várias categorias.

<details>
  <summary>Exemplos de Categoria de Meme</summary>

### Reconhecimento

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/ack/isla_plastic_memories.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/ok/Hachikuji_ok.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/thumbs_up/thumbs_up_one.gif) |

### Alerta

Algo Aconteceu!

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/alert/cat_ears_wiggle_one.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/alert/pointing_two.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/alert/finger_guns_one.gif) |

### Entediado

Você foi embora por um tempo, volte!

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/bored/karyl-kyaru_waiting.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/bored/spy-x-family-spy-family_anya_wait.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/bored/bored_three3.gif) |

### Celebração

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/approval/zero_two_nodding.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/success/spear_yeet_full.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/celebration/caramelldansen.gif) |

### Decepção (provavelmente a mais usada categoria)

por que você fez isso?

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/crying/crying_reaction_four.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/crying/crying_reaction_six.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/dissapointment/yotsuba_no.gif) |

### Enfurecido

corra e encontre cobertura!

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/enraged/yuno_snapped.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/enraged/demon_rem.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/enraged/hayase-nagatoro-nagatoro-angry.gif) |

### Frustração

eu estou bravo

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/table_slam/aqua_anime-konosuba.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/frustration/frustration_two.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/frustrated/frustration_male_one.gif) |

### Feliz

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/excited/jahy-jahysama_4.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/happy/ilulu-maid-dragon.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/happy/tonikaku-kawaii-tonikaku.gif) |

### Zombando

YA DUN MESSED UP A-A-RON!!!1

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/amused/zero_two_amused.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/mocking/rin_mocking.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/mocking/aqua_mocking_laugh.gif) |

### Motivação

Quando você precisa de apenas um pouco de empurrão na direção certa

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/thumsb_up/natsuko_honda_thumbs.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/thumbs_up/kakashi_thumbs_up.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/pointing/acknowledgment_one.gif) |

### Esperando Pacientemente

Quando você foi embora por um pouco.

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/waiting/senko-waiting.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/waiting/aleh.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/waiting/narumi-wotaku.gif) |

### Fazendo Bico

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/pout/yotsuba_pout.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/pouting/itsuki_pouting_two.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/pout/ichika_pout.gif) |

### Chocado

Quando você tem ido bem por um tempo, e você quebra algo.

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/shocked/rikka_shocked.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/shocked/hifumi_surprised.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/shocked/ram_rem_shocked.gif) |

### Arrogante

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/smug/kyaru-sky.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/smug/love-blushing.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/smug/utaha-saekano.gif) |

### Cansado

Quando você foi embora por um tempo realmente longo. Você viu esses um monte :)

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/tired/princess-connect-priconne_sleep.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/tired/senko-sleep.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/tired/miss-kobayashi-dragon-maid-s-ilulu.gif) |

### Recepcionando

Sempre que você abre um novo projeto

| Exemplo Um | Exemplo Dois | Exemplo Três|
| --- | --- | --- |
| ![exemploUm](https://amii.assets.unthrottled.io/visuals/welcome/miia_greeting.gif) | ![exemploDois](https://amii.assets.unthrottled.io/visuals/welcome/mana_hello.gif) | ![exemploTrês](https://amii.assets.unthrottled.io/visuals/welcome/chika_hey.gif) |

</details>


### Como fazer:

1. Coloque imagens `GIF` no seu `Diretório de Assets Personalizados` especificado.
2. Abra o menu de configurações `Conteúdo Personalizado`.
3. Use o `Mostrar apenas itens não marcados`, para filtrar a lista para memes que precisam ser marcados.
4. Adicione uma categoria aos seus assets não marcados.
5. Use o botão de atualização `Ler Assets` para pegar quaisquer mudanças novas enquanto o menu de configurações estiver aberto

**Nota**: Você pode renomear assets, você apenas tem que usar o `Reescanear diretório de assets personalizados` para MIKU obter o caminho de arquivo atualizado.
Caso contrário, esse meme não funcionará mais quando MIKU tentar usá-lo.

#### Reescanear

Isso permite que MIKU pegue quaisquer novos assets que você adiciona ao seu diretório de assets personalizados.
Como é uma operação cara, MIKU só fará uma varredura completa/recursiva do seu diretório de assets personalizados quando:

- Seu IDE inicia pela primeira vez
- Você abre a aba do menu de configurações `Assets Personalizados`.
- Você aciona a ação `Reescanear diretório de assets personalizados`.

### Rotulagem Automática

Como é muito trabalho usar um menu para marcar assets e a maioria dos assets pertence a uma categoria, eu imaginei que este recurso
poderia ser útil.

Quando `Criar Diretórios Auto Marcados` estiver habilitado, MIKU criará todos os diretórios, associados a uma categoria específica de asset no seu diretório de assets personalizados.

Apenas adicione memes aos diretórios apropriados e quando MIKU varrer os diretórios de assets personalizados, eles adicionarão automaticamente a categoria correspondente ao asset.

### Conteúdo Sugestivo

Eu construí principalmente o recurso `Assets Personalizados` porque eu sou um degenerado.
Dessa forma eu posso ter conteúdo de anime picante, sem me preocupar com o plugin sendo removido por quebrar algum termo de serviço. Significa que este plugin nunca virá empacotado com qualquer conteúdo NSFW, mas você pode adicionar se quiser!

Então se você também é um indivíduo de cultura você pode marcar vários assets como `Sugestivo`.
Se você usar `Rotulagem Automática` (eu não vejo por que você não usaria), haverá um diretório `suggestive` criado.
No diretório `suggestive`, você verá subdiretórios que correspondem às mesmas categorias no nível superior.

Aqueles diretórios funcionam da mesma maneira que os diretórios de auto-tag regulares. Apenas coloque seus assets naqueles diretórios.
Quando MIKU varrer seu diretório de assets personalizados, eles marcarão automaticamente as categorias & marcarão o asset como `Sugestivo`.

Eu também adicionei a ação `Alternar modo sugestivo` que permite alternar rapidamente para aparecer como um weeb puro e inocente.
Nenhum asset marcado como sugestivo aparecerá (mesmo no menu de configurações).
Quando a costa estiver clara, você pode voltar a ser um degenerado.

# Configuração

`Tools | AMII Options | Show AMII's Settings`

De qualquer jeito que você quiser, esse é o jeito que você precisa!
AMII tem muita personalização que permite adaptar a experiência às suas preferências.

## Som

Você não ouviu? Bem se você não ouviu, algumas das suas interações com MIKU podem envolver um clipe de som relacionado.

Nem todo mundo quer ter sua música interrompida enquanto está programando, então você pode desligar todo o som. Você até tem a capacidade de aumentar e abaixar o volume também!

## Conteúdo

**Gênero Preferido**

Todos nós temos nossos gostos e desgostos (waifus, husbandos, robôs gigantes, etc), bem MIKU tem traços para diferentes tipos de pessoas. Esta seção só mostrará memes que contenham **qualquer** do gênero preferido.

**Personagens Preferidos**

Apenas quer ver conteúdo do seu crush principal? Bem você pode gentilmente pedir ao MIKU para mostrar apenas imagens do seu personagem favorito. No entanto, seu personagem favorito pode **não** estar em um asset que MIKU pode usar para expressar seus sentimentos.
Em vez de não obter nada, você obterá outra imagem aleatória que corresponda às suas outras preferências!

**Personagens na Lista Negra**

Você não gosta das mesmas coisas que eu gosto?? A audácia, como você ousa!

Estou brincando! 😃

Eu meio que imaginei que isso poderia acontecer, então eu também adicionei uma `Lista Negra de Personagens`. Que impede _qualquer_ conteúdo contendo os personagens selecionados de aparecer!

**Nota**: a **lista negra** tem preferência sobre **preferências**. Então se houver conteúdo com personagens na lista negra
e personagens preferidos, bem então você não obterá esse conteúdo mostrado a você.

## Exibição

MIKU tem que colocar seus memes em algum lugar na tela do seu IDE. Então aqui está como você pode solicitar para ter seus memes trabalhando do jeito que você quer.

**Posição**

Cada bloco representa uma seção onde você quer que seu meme seja ancorado na tela do seu IDE.

### Demissão

Memes têm que vir e ir, se eles não saíssem então, seria um pouco difícil fazer qualquer trabalho. Aqui está o que cada modo faz.

**Cronometrado**

MIKU quer que cada meme circule pelo menos uma vez. Alguns memes têm uma duração mais longa do que outros. Aqui você pode especificar a quantidade mínima de tempo que você quer que cada meme apareça na tela.

> **Dica**: Se você quiser que seu meme `cronometrado` fique por mais tempo, apenas clique no meme!
> Isso converterá o modo de demissão para `perda de foco`.
> Também útil para fazer memes longos desaparecerem mais cedo!

**Perda de Foco**

Em vez de deixar MIKU decidir sobre a duração do seu meme, coloque-se no controle. Memes criados com a opção de demissão de perda de foco só desaparecerão quando você começar a programar ou clicar fora do meme.

Como o meme desaparece quando você está trabalhando, às vezes você pode dispensar acidentalmente seu meme. Então cada meme recebe uma duração configurável onde eles são invulneráveis à demissão. Que deve lhe dar tempo suficiente para parar e apreciar!

**Limitação de Dimensão**

Alguns memes, fornecidos por este plugin, são grandes e às vezes podem atrapalhar. Felizmente, se você achar que este é o caso, você pode limitar as dimensões máximas dos memes a serem exibidos.

MIKU quer manter a proporção original da imagem, então eles tomarão a maior dimensão e a limitarão a
aquele. Dessa forma você ainda pode ver a mesma imagem, apenas menor.

Aqui está uma amostra de definir as dimensões limitadas a 200 largura e altura.

![Limitação de Dimensão](readmeAssets/dimension_capping.gif)

**Nota**: se você não quiser que ambas as dimensões sejam limitadas (apenas uma, quando habilitado), apenas use um `-1` como o valor. Dessa forma MIKU sabe ignorar essa dimensão ao calcular dimensões redimensionadas.

## Sincronização de Assets

`Tools | AMII Options | Sincronize Assets`

Você pediu para ter um novo asset adicionado?

Bem você pode começar a usar esse asset imediatamente, usando esta ação. Isso atualiza suas listas locais de assets disponíveis para serem as mais atuais.

**Auto-Sinc**: AMII é programado para atualizar automaticamente uma vez por dia, para trazer os memes de anime mais frescos e dankest na régua.

### Visualização de Assets

Você sabia que você pode ver todos os assets que AMII usa
aqui: [https://amii-assets.unthrottled.io/](https://amii-assets.unthrottled.io/)?

---

# Extras!

<div align="center">
    <img src="https://doki.assets.unthrottled.io/misc/logo.svg" ></img>
</div>

## O Tema Doki

Você precisa de mais waifus de anime na sua vida? Bem eu tenho uma solução apenas para esse
problema, [O Tema Doki](https://github.com/doki-theme/doki-theme)!
Decore todas as suas ferramentas favoritas com seu(s) personagem(ns) favorito(s)!

Disponível para qualquer [JetBrains IDE](https://github.com/doki-theme/doki-theme-jetbrains).

![Tema Doki Jetbrains](https://github.com/doki-theme/doki-theme-jetbrains/raw/master/assets/screenshots/themes.webp)

## Waifu Motivator

<p align="center"><img src="https://raw.githubusercontent.com/waifu-motivator/waifu-motivator-plugin/master/images/wmp_logo.png" height="256px" alt="Waifu Motivator Plugin Logo"></p>

<p align="center">Uma coleção de plugins de IDE Jetbrains de código aberto que trazem <i>Waifus</i> para ajudar a manter sua motivação para completar durante seus desafios de codificação.</p>

Disponível para qualquer [JetBrains IDE](https://github.com/waifu-motivator/waifu-motivator-plugin).

## Quer atualizações do AMII mais cedo?

Eu tenho um [canal de lançamento canary](https://github.com/Unthrottled/jetbrains-plugin-repository) que você pode configurar para obter
o mais recente e maior!

---

# Atribuições

Projeto usa ícones de [Twemoji](https://github.com/twitter/twemoji). Gráficos licenciados sob CC-BY
4.0: https://creativecommons.org/licenses/by/4.0/

Plugin baseado no [Modelo de Plugin da Plataforma IntelliJ](https://github.com/JetBrains/intellij-platform-plugin-template)
