#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <Preferences.h>
#include <driver/adc.h>
#include "esp_adc_cal.h"
#include <FastLED.h>
#define U8G2_16BIT
#include <U8g2lib.h>

U8G2_SSD1306_128X64_NONAME_F_HW_I2C oled(U8G2_R0);


Preferences prefs;
BLEServer *pServer = NULL;
BLEAdvertising *pAdvertising;


#define PREFS_APPNAME "icpcbadge"
#define PREFS_BADGETEXT_KEY "badgetext"
#define PREFS_FIRSTNAME_KEY "firstname"
#define PREFS_FIRSTNAME_DEFAULT "John"
#define PREFS_LASTNAME_KEY "lastname"
#define PREFS_LASTNAME_DEFAULT "Smith"

#define RESET_BUTTON A0
#define RGB_LED_DIN GPIO_NUM_27
#define RGB_LED_COUNT 1

CRGB leds[RGB_LED_COUNT];

//BLE UUID's
#define BT_USER_DATA_SERVICE 0x181C
#define BT_USER_DATA_FNAME 0x2A8A
#define BT_USER_DATA_LNAME 0x2A90
#define BT_RGB_LED_SERVICE "6d7df50f-3732-458a-b9fe-929df18f12a8"
#define BT_RGB_LED_COLOR "8fb3bf3c-8448-455c-ae9e-93905b7bd41e"
#define BT_BADGE_MESSAGE_SERVICE "7f40c29a-b34a-4aca-b5d0-53606b6fe538"
#define BT_BADGE_MESSAGE_CHAR "563d1394-3282-4262-88cb-677962b7e69a"
//<end> BLE UUID's


enum DRAWMODE_T
{
    MODE_NAMETAG,
    MODE_BADGESTRING
};
DRAWMODE_T DRAWMODE;
void updateDisplay();
void scrollOnce(const String&);

bool BT_CONNECTED = false;

class btServerCallbacks : public BLEServerCallbacks
{
    void onConnect(BLEServer *pServer) override {
        BT_CONNECTED = true;
    }

    void onDisconnect(BLEServer *pServer) override {
        BT_CONNECTED = false;
    }
};

class ICPCBadgeBLECallbacks : public BLECharacteristicCallbacks
{
public:
    void onWrite(BLECharacteristic *pChar) override {
        BLEUUID uuid = pChar->getUUID();
        prefs.begin(PREFS_APPNAME, false);
        String val = String(pChar->getValue().c_str());
        if (uuid.equals(BLEUUID((uint16_t)BT_USER_DATA_FNAME)))
        {
            prefs.putString(PREFS_FIRSTNAME_KEY, val);
        }
        else if (uuid.equals(BLEUUID((uint16_t)BT_USER_DATA_LNAME)))
        {
            prefs.putString(PREFS_LASTNAME_KEY, val);
        }
        else if (uuid.equals(BLEUUID(BT_BADGE_MESSAGE_CHAR)))
        {
            prefs.putString(PREFS_BADGETEXT_KEY, val);
            DRAWMODE = MODE_BADGESTRING;
        }
        else if (uuid.equals(BLEUUID(BT_RGB_LED_COLOR)))
        {
            uint32_t c = 0;
            const char* chrs = pChar->getValue().c_str();

            auto r = (uint8_t) chrs[1];
            auto g = (uint8_t) chrs[2];
            auto b = (uint8_t) chrs[3];
            c = (r << 16) | (g << 8) | (b << 0);
            // Serial.println(pChar->getValue().c_str());
            for (auto &led : leds) {
                led = CRGB(r, g, b);
            }
            FastLED.show();
        }
        prefs.end();
    }
    void onRead(BLECharacteristic *pChar) override {
        BLEUUID uuid = pChar->getUUID();
        prefs.begin(PREFS_APPNAME, true);
        if (uuid.equals(BLEUUID((uint16_t)BT_USER_DATA_FNAME)))
        {
            String val = prefs.getString(PREFS_FIRSTNAME_KEY, "John");
            pChar->setValue(val.c_str());
        }
        else if (uuid.equals(BLEUUID((uint16_t)BT_USER_DATA_LNAME)))
        {
            String val = prefs.getString(PREFS_LASTNAME_KEY, "Smith");
            pChar->setValue(val.c_str());
        }
        else if (uuid.equals(BLEUUID(BT_RGB_LED_COLOR)))
        {
            uint32_t c = 0;
            leds[0].
            c = (leds[0].red << 16) | (leds[0].green << 8) | (leds[0].blue << 0);
            pChar->setValue(c);
        }
        else
        {
            pChar->setValue("Characteristic read not implemented");
        }
        prefs.end();
    }
};

void setup()
{
    Serial.begin(9600);
    pinMode(RESET_BUTTON, INPUT_PULLDOWN);
    FastLED.addLeds<APA106, RGB_LED_DIN>(leds, RGB_LED_COUNT);
    leds[0] = CRGB::Black;
    FastLED.show();
    oled.begin();
    oled.clearBuffer();
    oled.setFont(u8g2_font_inb30_mr);
    oled.drawStr(0, 30, "ICPC");
    oled.drawStr(0, 60, "BADGE");
    oled.sendBuffer();
    delay(1000);

    //Bluetooth stuffs
    {
        Serial.println("Starting BLE work!");
        BLEDevice::init("icpc_badge_01");
        pServer = BLEDevice::createServer();
        pServer->setCallbacks(new btServerCallbacks());
        BLEService *pUserDataService = pServer->createService((uint16_t)BT_USER_DATA_SERVICE);
        BLEService *pBadgeService = pServer->createService(BT_BADGE_MESSAGE_SERVICE);
        BLEService *pLedService = pServer->createService(BT_RGB_LED_SERVICE);
        BLECharacteristic *pUserFName = pUserDataService->createCharacteristic(
                (uint16_t)BT_USER_DATA_FNAME,
                BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
        BLECharacteristic *pUserLName = pUserDataService->createCharacteristic(
                (uint16_t)BT_USER_DATA_LNAME,
                BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
        BLECharacteristic *pBadgeString = pBadgeService->createCharacteristic(
                BT_BADGE_MESSAGE_CHAR,
                BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
        BLECharacteristic *pLedColorChar = pLedService->createCharacteristic(
                BT_RGB_LED_COLOR,
                BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
        pUserFName->setCallbacks(new ICPCBadgeBLECallbacks());
        pUserLName->setCallbacks(new ICPCBadgeBLECallbacks());
        pBadgeString->setCallbacks(new ICPCBadgeBLECallbacks());
        pLedColorChar->setCallbacks(new ICPCBadgeBLECallbacks());
        pUserDataService->start();
        pBadgeService->start();
        pLedService->start();
        pAdvertising = pServer->getAdvertising();
        pAdvertising->start();
        Serial.println("Enabled Advertising");
    }
    updateDisplay();
}

void loop()
{
    switch (DRAWMODE)
    {
        case (MODE_BADGESTRING):
        {
            prefs.begin(PREFS_APPNAME, false);
            String text = prefs.getString(PREFS_BADGETEXT_KEY, "<asdf>");
            prefs.end();
            scrollOnce(text);
            DRAWMODE = MODE_NAMETAG;
        }
        case (MODE_NAMETAG):
            updateDisplay();
            break;
    }
    delay(100);
    int duration_held = 0;
    while (digitalRead(RESET_BUTTON))
    {
        delay(10);
        duration_held += 10;
        if (duration_held >= 3000)
        {
            prefs.begin(PREFS_APPNAME, false);
            prefs.clear();
            prefs.putString(PREFS_FIRSTNAME_KEY, PREFS_FIRSTNAME_DEFAULT);
            prefs.putString(PREFS_LASTNAME_KEY, PREFS_LASTNAME_DEFAULT);
            prefs.end();
            oled.clearBuffer();
            oled.setFont(u8g2_font_inb30_mr);
            oled.drawStr(0, 30, "DATA");
            oled.drawStr(0, 60, "RESET");
            oled.sendBuffer();
            delay(1000);
            break;
        }
    }
}

void scrollOnce(const String &text)
{
    const uint8_t *font = u8g2_font_inb30_mr;
    int y = 50;
    int scrollspeed = 3;       // pixels per 10ms
    int char_pixel_width = 25; // dependent on font
    int displ_width = 128;
    if (text.length() > 5)
    {
        int starting_position;
        for (unsigned int i = 0; i < text.length(); i++)
        {
            String sub = text.substring(i, i + 6);
            if (i == 0)
                starting_position = displ_width - char_pixel_width;
            else
                starting_position = 0;
            int x = 0;
            do
            {
                oled.clearBuffer();
                oled.setFont(font);
                oled.drawStr(static_cast<u8g2_uint_t>(x + starting_position), static_cast<u8g2_uint_t>(y), sub.c_str());
                oled.sendBuffer();
                x = x - scrollspeed;
                delay(1);
            } while (x > -(char_pixel_width + starting_position));
        }
    }
    else
    {
        int x = 0;
        int str_pixel_width = char_pixel_width * text.length();
        do
        {
            oled.clearBuffer();
            oled.setFont(font);
            oled.drawStr(static_cast<u8g2_uint_t>(x + displ_width), static_cast<u8g2_uint_t>(y), text.c_str());
            oled.sendBuffer();
            x = x - scrollspeed;
            delay(1);
        } while (x > -(str_pixel_width + displ_width));
    }
}

void updateDisplay()
{
    prefs.begin(PREFS_APPNAME, false);
    String firstname = prefs.getString(PREFS_FIRSTNAME_KEY, "John");
    String lastname = prefs.getString(PREFS_LASTNAME_KEY, "Smith");
    //Convert adc_reading to voltage in mV
    // uint32_t voltage = esp_adc_cal_raw_to_voltage(adc_reading, adc_chars);
    float voltage = ((3.3f * float(analogRead(GPIO_NUM_35)) / 4096.0f) + 0.2f) * 2;
    String battlevel = "Batt: " + String(voltage) + "V   BT:" + ((BT_CONNECTED) ? "YES" : "NO");
    prefs.end();
    oled.clearBuffer();
    oled.setFont(u8g2_font_helvB08_tr);
    oled.drawStr(0, 8, battlevel.c_str());
    oled.setFont(u8g2_font_helvB14_tr);
    oled.drawStr(0, 30, firstname.c_str());
    oled.drawStr(0, 61, lastname.c_str());
    oled.sendBuffer();
}
