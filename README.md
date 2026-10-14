# RokiaArcade 0.1.0

Three offline time-killers for Java ME (CLDC 1.1 / MIDP 2.0) in one
JAR: **Snake**, **Breakout**, **2048**. D-pad only, FIRE pauses with
resume/quit, best scores in RMS, 0 mutes, red key exits.

## Games

- Snake: 20x22 board, no 180s, speed ramps with length, walls + self kill.
- Breakout: hold LEFT/RIGHT, FIRE launches, paddle aim by hit offset,
  3 lives, endless speeding levels.
- 2048: d-pad slides, merge-once rule, win at 2048 then keep going.

## Tech

Built on Nitro2D (`Game` loop, `Sfx`) + J2CN-free rendering: every
game draws with raw `Graphics` rects (no atlas, tiny heap). Pure
logic cores (`SnakeLogic`, `BreakLogic`, `Grid2048`) carry zero LCDUI
and run headless in desktop tests; key input polls per sim step
because the engine suppresses key events. Font is the Inter Tight
bitmap from J2CN.

## Build

```sh
./build.sh      # dist/RokiaArcade.jar + .jad, class version 47
./test/run.sh   # device build + logic tests
```

MIT — do what you want, no warranty.
