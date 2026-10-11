-- matchmaking.lua
-- KEYS[1]: Matchmaking Queue key (ZSET)
-- ARGV[1]: Current Player ID (String)
-- ARGV[2]: Current Player Elo (Number)
-- ARGV[3]: Min Elo Range (Number)
-- ARGV[4]: Max Elo Range (Number)

local queueKey = KEYS[1]
local playerId = ARGV[1]
local playerElo = tonumber(ARGV[2])
local minElo = tonumber(ARGV[3])
local maxElo = tonumber(ARGV[4])

-- 1. Tìm các ứng viên đối thủ trong khoảng Elo [minElo, maxElo]
local candidates = redis.call('ZRANGEBYSCORE', queueKey, minElo, maxElo, 'LIMIT', 0, 10)

for _, candidate in ipairs(candidates) do
    if candidate ~= playerId then
        -- 2. Đã tìm thấy đối thủ hợp lệ! Rút candidate và playerId ra khỏi ZSET ngay lập tức
        redis.call('ZREM', queueKey, candidate)
        redis.call('ZREM', queueKey, playerId)
        return "MATCHED:" .. candidate
    end
end

-- 3. Chưa tìm thấy đối thủ thích hợp -> Thêm hoặc cập nhật playerId vào ZSET
redis.call('ZADD', queueKey, playerElo, playerId)
return "QUEUED"
