#define LED LED_BUILTIN

const int DOT = 120;
const int DASH = DOT * 3;
const int SYMBOL_GAP = DOT;
const int LETTER_GAP = DOT * 3;
const int WORD_GAP = DOT * 7;

void ledOn(int ms) {
  digitalWrite(LED, HIGH);
  delay(ms);
  digitalWrite(LED, LOW);
}

void playScript(const String& script) {
  for (int i = 0; i < script.length(); i++) {
    char c = script[i];

    if (c == '.') {
      ledOn(DOT);
      delay(SYMBOL_GAP);
    } else if (c == '-') {
      ledOn(DASH);
      delay(SYMBOL_GAP);
    } else if (c == ' ') {
      delay(LETTER_GAP);
    } else if (c == '/') {
      delay(WORD_GAP);
    } else {
      /* no-op */
    }
  }
  digitalWrite(LED, LOW);
}

String readLine() {
  static String buf;

  while (Serial.available() > 0) {
    char ch = (char)Serial.read();
    if (ch == '\r') continue;

    if (ch == '\n') {
      String line = buf;
      buf = "";
      line.trim();
      return line;
    }

    buf += ch;
    if (buf.length() > 512) buf.remove(0, buf.length() - 512);
  }

  return "";
}

void setup() {
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW);

  Serial.begin(115200);
  Serial.println("READY");
}

void loop() {
  String line = readLine();
  if (line.length() == 0) return;

  if (line == "PING") {
    Serial.println("PONG");
    return;
  }

  if (line == "BYE") {
    Serial.println("BYE");
    return;
  }

  if (line.startsWith("PLAY:")) {
    String script = line.substring(5);

    Serial.println("OK");
    playScript(script);
    Serial.println("DONE");
    return;
  }

  Serial.print("ERR:UNKNOWN_CMD ");
  Serial.println(line);
}
