# BOX GAME BRIDGE 1.0

Protótipo Java para Arch Linux que conecta o Undertale no computador à TFT ST7735 ligada ao Arduino Uno.

## O que faz

- Procura uma janela do Undertale usando `wmctrl`.
- Detecta portas seriais `/dev/ttyACM*` e `/dev/ttyUSB*`.
- Captura a janela do jogo e reduz a imagem para 160 × 128.
- Divide a imagem em blocos de 8 × 8 e envia apenas os blocos alterados.
- Recebe o joystick pelo USB serial e converte direções em setas; o botão SW equivale a Enter.
- Meta inicial de atualização: até 10 FPS (depende da quantidade de blocos alterados e da velocidade serial).

## Requisitos no Arch Linux

```bash
sudo pacman -S jdk-openjdk wmctrl xdotool
```

## Compilar e executar

```bash
javac BoxGameBridge.java
java BoxGameBridge
```

Ou executar o aplicativo compilado:

```bash
java -jar BOX_GAME_BRIDGE_1_0.jar
```

## Uso

1. Abra Undertale em uma janela visível, sem minimizá-la.
2. Inicie o BOX GAME BRIDGE.
3. Clique em **Procurar Undertale**.
4. Clique em **Procurar Arduino** e selecione a porta correta.
5. Clique em **Conectar Arduino**.
6. Clique em **Iniciar transmissão**.

## Protocolo serial

- Baud rate: 500000.
- Arduino → PC: linha ASCII `J:HH\n`, onde os bits 0 a 4 representam cima, baixo, esquerda, direita e Enter/SW.
- PC → Arduino: pacote binário iniciado por `BX`, versão 1, tipo 1, sequência de 16 bits, quantidade de blocos e, para cada bloco, coordenadas e 64 pixels RGB565.
- Cada bloco mede 8 × 8 pixels. A imagem enviada tem 160 × 128 pixels em orientação paisagem.

**Importante:** este repositório contém o aplicativo do computador. O firmware correspondente do Arduino ainda precisa ser instalado para receber os pacotes de imagem e transmitir os comandos do joystick.

## Limitações conhecidas

- A captura de janela depende de X11/XWayland, `wmctrl` e `xdotool`. Em Wayland puro, a localização/captura pode não funcionar.
- A janela do jogo precisa permanecer visível e não pode ficar coberta por outra janela.
- O Uno deve deixar RX/TX (D0/D1) livres durante o uso.
- 10 FPS é uma meta inicial, não uma garantia. O desempenho real depende da quantidade de blocos que mudam e da conexão serial.
