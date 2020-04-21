//
// Created by HW on 2020/4/20.
//

#include "BaseLog.h"

static int sBaseLogLevel = BASE_LOG_LEVEL_ALL;

void BASE_SetLogLevel(int level) {
    sBaseLogLevel = level;
}

int BASE_GetLogLevel() {
    return sBaseLogLevel;
}