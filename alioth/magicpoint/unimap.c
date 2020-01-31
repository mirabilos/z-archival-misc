/*
 * Copyright (C) 1997 and 1998 WIDE Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#include "mgp.h"

void
latin_unicode_map_init(void)
{

latin_unicode_map[0][0xA1] = 0x0104; latin_unicode_map[0][0xA2] = 0x02D8;
latin_unicode_map[0][0xA3] = 0x0141; latin_unicode_map[0][0xA5] = 0x013D;
latin_unicode_map[0][0xA6] = 0x015A; latin_unicode_map[0][0xA9] = 0x0160;
latin_unicode_map[0][0xAA] = 0x015E; latin_unicode_map[0][0xAB] = 0x0164;
latin_unicode_map[0][0xAC] = 0x0179; latin_unicode_map[0][0xAE] = 0x017D;
latin_unicode_map[0][0xAF] = 0x017B; latin_unicode_map[0][0xB1] = 0x0105;
latin_unicode_map[0][0xB2] = 0x02DB; latin_unicode_map[0][0xB3] = 0x0142;
latin_unicode_map[0][0xB5] = 0x013E; latin_unicode_map[0][0xB6] = 0x015B;
latin_unicode_map[0][0xB7] = 0x02C7; latin_unicode_map[0][0xB9] = 0x0161;
latin_unicode_map[0][0xBA] = 0x015F; latin_unicode_map[0][0xBB] = 0x0165;
latin_unicode_map[0][0xBC] = 0x017A; latin_unicode_map[0][0xBD] = 0x02DD;
latin_unicode_map[0][0xBE] = 0x017E; latin_unicode_map[0][0xBF] = 0x017C;
latin_unicode_map[0][0xC0] = 0x0154; latin_unicode_map[0][0xC3] = 0x0102;
latin_unicode_map[0][0xC5] = 0x0139; latin_unicode_map[0][0xC6] = 0x0106;
latin_unicode_map[0][0xC8] = 0x010C; latin_unicode_map[0][0xCA] = 0x0118;
latin_unicode_map[0][0xCC] = 0x011A; latin_unicode_map[0][0xCF] = 0x010E;
latin_unicode_map[0][0xD0] = 0x0110; latin_unicode_map[0][0xD1] = 0x0143;
latin_unicode_map[0][0xD2] = 0x0147; latin_unicode_map[0][0xD5] = 0x0150;
latin_unicode_map[0][0xD8] = 0x0158; latin_unicode_map[0][0xD9] = 0x016E;
latin_unicode_map[0][0xDB] = 0x0170; latin_unicode_map[0][0xDE] = 0x0162;
latin_unicode_map[0][0xDF] = 0x00DF; latin_unicode_map[0][0xE0] = 0x0155;
latin_unicode_map[0][0xE3] = 0x0103; latin_unicode_map[0][0xE5] = 0x013A;
latin_unicode_map[0][0xE6] = 0x0107; latin_unicode_map[0][0xE8] = 0x010D;
latin_unicode_map[0][0xEA] = 0x0119; latin_unicode_map[0][0xEC] = 0x011B;
latin_unicode_map[0][0xEF] = 0x010F; latin_unicode_map[0][0xF0] = 0x0111;
latin_unicode_map[0][0xF1] = 0x0144; latin_unicode_map[0][0xF2] = 0x0148;
latin_unicode_map[0][0xF5] = 0x0151; latin_unicode_map[0][0xF8] = 0x0159;
latin_unicode_map[0][0xF9] = 0x016F; latin_unicode_map[0][0xFB] = 0x0171;
latin_unicode_map[0][0xFE] = 0x0163; latin_unicode_map[0][0xFF] = 0x02D9;

latin_unicode_map[1][0xA1] = 0x0126; latin_unicode_map[1][0xA2] = 0x02D8;
latin_unicode_map[1][0xA6] = 0x0124; latin_unicode_map[1][0xA9] = 0x0130;
latin_unicode_map[1][0xAA] = 0x015E; latin_unicode_map[1][0xAB] = 0x011E;
latin_unicode_map[1][0xAC] = 0x0134; latin_unicode_map[1][0xAF] = 0x017B;
latin_unicode_map[1][0xB1] = 0x0127; latin_unicode_map[1][0xB6] = 0x0125;
latin_unicode_map[1][0xB9] = 0x0131; latin_unicode_map[1][0xBA] = 0x015F;
latin_unicode_map[1][0xBB] = 0x011F; latin_unicode_map[1][0xBC] = 0x0135;
latin_unicode_map[1][0xBF] = 0x017C; latin_unicode_map[1][0xC5] = 0x010A;
latin_unicode_map[1][0xC6] = 0x0108; latin_unicode_map[1][0xD5] = 0x0120;
latin_unicode_map[1][0xD8] = 0x011C; latin_unicode_map[1][0xDD] = 0x016C;
latin_unicode_map[1][0xDE] = 0x015C; latin_unicode_map[1][0xDF] = 0x00DF;
latin_unicode_map[1][0xE5] = 0x010B; latin_unicode_map[1][0xE6] = 0x0109;
latin_unicode_map[1][0xF5] = 0x0121; latin_unicode_map[1][0xF8] = 0x011D;
latin_unicode_map[1][0xFD] = 0x016D; latin_unicode_map[1][0xFE] = 0x015D;
latin_unicode_map[1][0xFF] = 0x02D9;

latin_unicode_map[2][0xA1] = 0x0104; latin_unicode_map[2][0xA2] = 0x0138;
latin_unicode_map[2][0xA3] = 0x0156; latin_unicode_map[2][0xA5] = 0x0128;
latin_unicode_map[2][0xA6] = 0x013B; latin_unicode_map[2][0xA9] = 0x0160;
latin_unicode_map[2][0xAA] = 0x0112; latin_unicode_map[2][0xAB] = 0x0122;
latin_unicode_map[2][0xAC] = 0x0166; latin_unicode_map[2][0xAE] = 0x017D;
latin_unicode_map[2][0xB1] = 0x0105; latin_unicode_map[2][0xB2] = 0x02DB;
latin_unicode_map[2][0xB3] = 0x0157; latin_unicode_map[2][0xB5] = 0x0129;
latin_unicode_map[2][0xB6] = 0x013C; latin_unicode_map[2][0xB7] = 0x02C7;
latin_unicode_map[2][0xB9] = 0x0161; latin_unicode_map[2][0xBA] = 0x0113;
latin_unicode_map[2][0xBB] = 0x0123; latin_unicode_map[2][0xBC] = 0x0167;
latin_unicode_map[2][0xBD] = 0x014A; latin_unicode_map[2][0xBE] = 0x017E;
latin_unicode_map[2][0xBF] = 0x014B; latin_unicode_map[2][0xC0] = 0x0100;
latin_unicode_map[2][0xC7] = 0x012E; latin_unicode_map[2][0xC8] = 0x010C;
latin_unicode_map[2][0xCA] = 0x0118; latin_unicode_map[2][0xCC] = 0x0116;
latin_unicode_map[2][0xCF] = 0x012A; latin_unicode_map[2][0xD0] = 0x0110;
latin_unicode_map[2][0xD1] = 0x0145; latin_unicode_map[2][0xD2] = 0x014C;
latin_unicode_map[2][0xD3] = 0x0136; latin_unicode_map[2][0xD9] = 0x0172;
latin_unicode_map[2][0xDD] = 0x0168; latin_unicode_map[2][0xDE] = 0x016A;
latin_unicode_map[2][0xDF] = 0x00DF; latin_unicode_map[2][0xE0] = 0x0101;
latin_unicode_map[2][0xE7] = 0x012F; latin_unicode_map[2][0xE8] = 0x010D;
latin_unicode_map[2][0xEA] = 0x0119; latin_unicode_map[2][0xEC] = 0x0117;
latin_unicode_map[2][0xEF] = 0x012B; latin_unicode_map[2][0xF0] = 0x0111;
latin_unicode_map[2][0xF1] = 0x0146; latin_unicode_map[2][0xF2] = 0x014D;
latin_unicode_map[2][0xF3] = 0x0137; latin_unicode_map[2][0xF9] = 0x0173;
latin_unicode_map[2][0xFD] = 0x0169; latin_unicode_map[2][0xFE] = 0x016B;
latin_unicode_map[2][0xFF] = 0x02D9;
}
