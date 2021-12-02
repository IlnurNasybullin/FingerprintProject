package org.example.app.utils;

import java.util.Objects;

public record FingerprintMatchResult(double score, double threshold) {

    public boolean isMatch() {
        return score >= threshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FingerprintMatchResult that = (FingerprintMatchResult) o;
        return Double.compare(that.score, score) == 0 && Double.compare(that.threshold, threshold) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, threshold);
    }
}
