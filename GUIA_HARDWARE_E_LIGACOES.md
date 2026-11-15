# 💙 BOX GAME BRIDGE 1.0
## Guia de hardware, componentes e instalação

> **Objetivo:** usar a TFT ligada ao Arduino como uma pequena tela externa para um jogo executado no computador. O PC processa o jogo e guarda os arquivos; o Arduino recebe a imagem e lê o joystick.

[![Placa](https://img.shields.io/badge/Placa-Arduino%20Uno%20R3-blue)](Arduino/)
[![Tela](https://img.shields.io/badge/Tela-ST7735%20128×160-13B8D5)](Arduino/)
[![Meta](https://img.shields.io/badge/Meta-inicial%2010%20FPS-7B61FF)](README.md)

---

## 📦 1. Componentes necessários

| Quantidade | Componente | Para que serve |
|---:|---|---|
| 1 | Arduino Uno R3 | Recebe a imagem e lê o controle |
| 1 | TFT SPI ST7735, 1,8", 128 × 160 | Mostra a imagem do jogo |
| 1 | Joystick de dois eixos com botão SW (ex.: KY-023) | Controla o jogo |
| 1 | Cabo USB compatível com o Uno | Liga o Arduino ao PC |
| Alguns | Fios jumper | Fazem as conexões |
| 1 | Computador com Arch Linux | Executa o jogo e o aplicativo Java |

### ⚠️ Segurança elétrica — leia antes de ligar

- Desconecte o Arduino do USB antes de trocar qualquer fio.
- Confira a tensão aceita pelo **seu modelo exato de TFT**. Alguns módulos aceitam sinais de 5 V; outros trabalham somente com lógica de 3,3 V.
- Se o módulo não tiver conversão de nível lógico e não aceitar sinais de 5 V, use um conversor de nível lógico de 5 V para 3,3 V nos sinais que saem do Uno.
- Não ligue o VCC da TFT em 5 V sem confirmar que o seu módulo aceita essa tensão.
- Mantenha a alimentação de 3,3 V que já foi usada no módulo somente se estiver dentro da especificação dele.
- Peça ajuda de um adulto para soldagem, fios danificados ou alterações na alimentação.

---

## 🖥️ 2. Ligações da TFT ST7735

Com a nova pinagem, o projeto usa **SPI por software**: MOSI em D5 e SCK em D6. Isso permite usar os pinos escolhidos, mas pode ser mais lento do que o SPI de hardware.

| Pino escrito na TFT | Arduino Uno | Observação |
|---|---|---|
| GND | GND | Terra comum |
| VCC | 3V3* | Confirmar a tensão exigida pelo módulo |
| CS | D2 | Seleção da tela |
| RST ou RES | D3 | Reset da tela |
| DC ou A0 | D4 | Dados/comandos |
| SDA ou MOSI | D5 | Dados SPI por software |
| SCL, SCK ou CLK | D6 | Clock SPI por software |
| LED ou BL | Conforme o módulo | Manter a ligação de backlight indicada pelo fabricante |

* Use 3,3 V apenas se essa for a tensão especificada para a alimentação do seu módulo. A tensão de alimentação e a tolerância dos sinais lógicos são coisas diferentes: confira ambas.

### Importante sobre os nomes dos pinos

- Na TFT SPI, o pino chamado **SDA normalmente é MOSI**. Aqui ele vai ao D5; não é o SDA I2C do Arduino A4.
- Para esta pinagem, SDA/MOSI da TFT vai ao D5 e SCK vai ao D6.
- A4 e A5 ficam reservados para os dois eixos do joystick.
- A tela ST7735 não precisa de MISO para receber os pixels.
- O leitor microSD não é usado pelo BOX GAME BRIDGE. Não é necessário ligá-lo.
- O BOX GAME BRIDGE **não usa o microSD**. O cartão pode ficar fora do módulo.

---

## 🕹️ 3. Ligações do joystick

| Pino do joystick | Arduino Uno |
|---|---|
| GND | GND |
| VCC ou +5V | 5V* |
| VRX | A5 |
| VRY | A4 |
| SW | D7 |

* Para um módulo joystick KY-023 comum. Confira a identificação do seu módulo antes de alimentar.

O firmware lê VRX em A5, VRY em A4 e o botão SW em D7. O botão **SW equivale a Enter** no jogo.

---

## 🔌 4. Cabo USB e pinos que precisam ficar livres

- Ligue o Arduino Uno ao computador pelo cabo USB.
- Deixe **D0/RX e D1/TX livres**: eles são usados pela comunicação serial USB do Uno.
- Não ligue buzzer, joystick ou outro componente nesses dois pinos durante o uso do BOX GAME BRIDGE.
- A comunicação serial inicial do projeto está configurada para **500000 baud**.

---

## 🧠 5. Como o sistema funciona

1. O Undertale roda normalmente no computador.
2. O aplicativo Java captura a janela do jogo e reduz a imagem para **160 × 128 pixels**.
3. O PC compara o quadro novo com o anterior.
4. O PC envia somente os blocos de **8 × 8 pixels** que mudaram.
5. O Arduino recebe esses blocos pela USB e atualiza a TFT.
6. O joystick envia os comandos ao PC, que os transforma nas setas e em Enter.

### 🎯 Meta de desempenho

A meta inicial é de **até 10 FPS**. Isso é uma meta de desenvolvimento, não uma garantia: o resultado real depende de quantos blocos mudam na imagem, da captura da janela e da velocidade efetiva da conexão serial.

O PC faz o processamento e guarda os arquivos e saves. O Arduino não precisa de cartão SD para este projeto.

---

## 📚 6. Bibliotecas do Arduino

Instale as bibliotecas abaixo pela Arduino IDE ou use o pacote ZIP do repositório:

| Biblioteca | Versão | Função |
|---|---:|---|
| Adafruit GFX Library | 1.12.6 | Funções gráficas |
| Adafruit ST7735 and ST7789 Library | 1.12.0 | Driver da TFT |
| Adafruit BusIO | 1.17.4 | Dependência de comunicação |

<details>
<summary><strong>📥 Abrir pacote completo de bibliotecas</strong></summary>

[Baixar BOX_GAME_BRIDGE_LIBRARIES.zip](Bibliotecas/BOX_GAME_BRIDGE_LIBRARIES.zip)

O pacote também está na pasta [Bibliotecas/](Bibliotecas/README.md), com ZIPs individuais e instruções.

Depois de extrair, copie as pastas das bibliotecas para:

```text
~/Arduino/libraries/
```

Reinicie a Arduino IDE.

</details>

### Bibliotecas que já vêm com o ambiente Arduino

- `SPI.h` faz parte do pacote de placas Arduino AVR usado pelo Uno.
- O joystick usa `analogRead()` e `digitalRead()`; não precisa de biblioteca adicional.

---

## 🐧 7. Programas necessários no Arch Linux

No Konsole, instale:

```bash
sudo pacman -S jdk-openjdk wmctrl xdotool coreutils
```

| Programa | Uso |
|---|---|
| Java JDK | Executar e compilar o aplicativo Java |
| wmctrl | Localizar a janela do Undertale |
| xdotool | Enviar as teclas para o jogo |
| stty (coreutils) | Configurar a porta serial |

O Java usa bibliotecas padrão do JDK; não é necessário baixar JAR de terceiros.

---

## 🗂️ 8. Organização do repositório

| Pasta/arquivo | Conteúdo |
|---|---|
| [Arduino/](Arduino/) | Firmware que será carregado no Uno |
| [PC/](PC/) | Aplicativo Java do computador |
| [Bibliotecas/](Bibliotecas/) | Bibliotecas e pacotes ZIP |
| [README.md](README.md) | Página principal do projeto |

### ⚠️ Estado do projeto

O firmware do Arduino ainda está em desenvolvimento. Antes de ligar tudo e iniciar o jogo, confira a pinagem, a tensão da TFT e compile o código correspondente na Arduino IDE.

---

<p align="center">
  <strong>BOX GAME BRIDGE 1.0</strong><br>
  <sub>O PC pensa. O Arduino entrega. A TFT mostra. 💙</sub>
</p>
