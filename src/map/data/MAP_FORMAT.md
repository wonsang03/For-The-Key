# 맵 데이터 파일 포맷 명세

## 파일 구조

### 스테이지 정보
```
@STAGE <스테이지_번호>
@NAME <스테이지_이름>
```

### 방(Room) 정의
```
@ROOM <room_id> <room_type>
@SIZE <width> <height>
@POS <grid_x> <grid_y>
@MAP
<타일맵 데이터 (height 줄)>
@CONNECT <방향> <연결된_room_id>
@END
```

---

## 태그 설명

### @STAGE
- **형식**: `@STAGE <숫자>`
- **설명**: 스테이지 번호 (1~5)
- **예시**: `@STAGE 1`

### @NAME
- **형식**: `@NAME <문자열>`
- **설명**: 스테이지 이름
- **예시**: `@NAME The Dungeon Entry`

### @ROOM
- **형식**: `@ROOM <room_id> <room_type>`
- **설명**: 방 정의 시작
- **room_id**: 0부터 시작하는 방 고유 번호
- **room_type**: START | NORMAL | ITEM | BOSS
- **예시**: `@ROOM 0 START`

### @SIZE
- **형식**: `@SIZE <width> <height>`
- **설명**: 방의 가로/세로 타일 개수
- **주의**: width와 height는 가변적 (방마다 다를 수 있음)
- **예시**: `@SIZE 15 10`

### @POS
- **형식**: `@POS <grid_x> <grid_y>`
- **설명**: 방의 그리드 상 위치 (맵 에디터용, 현재는 참고용)
- **예시**: `@POS 0 0`

### @MAP
- **형식**: `@MAP` 다음 줄부터 타일맵 데이터
- **설명**: height만큼의 줄에 타일 심볼로 구성
- **주의**: 각 줄은 정확히 width 길이여야 함

### @CONNECT
- **형식**: `@CONNECT <방향> <연결된_room_id>`
- **설명**: 다른 방과의 연결 정보
- **방향**: NORTH | SOUTH | EAST | WEST
- **예시**: `@CONNECT SOUTH 1`
- **주의**: 여러 개 작성 가능

### @END
- **형식**: `@END`
- **설명**: 방 정의 종료

---

## 타일 심볼

| 심볼 | 타입 | 설명 |
|------|------|------|
| `W` | WALL | 벽 (충돌 O) |
| `.` | FLOOR | 바닥 (충돌 X) |
| `D` | DOOR | 문 (방 전환) |
| `E` | EXIT | 다음 스테이지로 가는 문 |

---

## 방 타입 (RoomType)

| 타입 | 설명 |
|------|------|
| START | 플레이어 시작 방 |
| NORMAL | 일반 전투 방 |
| KEY | 열쇠가 있는 방 |
| EXIT | 다음 스테이지로 가는 방 |

---

## 주석

- `#`으로 시작하는 줄은 주석으로 무시됨
- 빈 줄도 무시됨

---

## 예시

```
@STAGE 1
@NAME Tutorial Stage

@ROOM 0 START
@SIZE 10 8
@POS 0 0
@MAP
WWWWWWWWWW
W........W
W........W
W........W
W........W
W........W
W....D...W
WWWWWWWWWW
@CONNECT SOUTH 1
@END

@ROOM 1 NORMAL
@SIZE 12 10
@POS 0 1
@MAP
WWWWWWWWWWWW
W.....D....W
W..........W
W..........W
W..........W
W..........W
W..........W
W..........W
W....D.....W
WWWWWWWWWWWW
@CONNECT NORTH 0
@CONNECT EAST 2
@END

@ROOM 2 EXIT
@SIZE 10 8
@POS 1 1
@MAP
WWWWWWWWWW
W........W
W........W
W...E....W
W........W
W........W
W.D......W
WWWWWWWWWW
@CONNECT WEST 1
@END
```

---

## 작성 규칙

1. **방 크기는 가변적**: 각 방마다 다른 크기 가능
2. **문(D) 위치**: 연결 방향에 맞는 벽에 배치
   - NORTH 연결 → 위쪽 벽에 D
   - SOUTH 연결 → 아래쪽 벽에 D
   - EAST 연결 → 오른쪽 벽에 D
   - WEST 연결 → 왼쪽 벽에 D
3. **Room ID**: 0부터 시작, 중복 없이 순차적
4. **연결은 양방향**: 방 A에서 B로 연결 시, B에서도 A로 연결 명시

---

## 향후 확장 가능성

나중에 추가될 수 있는 태그들:
- `@SPAWN ENEMY <x> <y>` - 적 스폰 위치
- `@SPAWN ITEM <x> <y> <item_type>` - 아이템 배치
- `@SPAWN PLAYER <x> <y>` - 플레이어 시작 위치
- `@PROPERTY <key> <value>` - 방 속성 (밝기, 배경 등)
