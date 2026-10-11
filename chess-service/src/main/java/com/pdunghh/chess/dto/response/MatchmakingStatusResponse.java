package com.pdunghh.chess.dto.response;

public record MatchmakingStatusResponse(
        String status, // "QUEUED", "MATCHED", "CANCELLED"
        String message,
        MatchFoundResponse match
) {
    public static MatchmakingStatusResponse queued(String message) {
        return new MatchmakingStatusResponse("QUEUED", message, null);
    }

    public static MatchmakingStatusResponse matched(MatchFoundResponse match) {
        return new MatchmakingStatusResponse("MATCHED", "Đã tìm thấy trận đấu!", match);
    }

    public static MatchmakingStatusResponse cancelled() {
        return new MatchmakingStatusResponse("CANCELLED", "Đã hủy tìm trận thành công.", null);
    }
}
