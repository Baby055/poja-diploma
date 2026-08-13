package hei.poja.io.model;

import lombok.Builder;
import org.aspectj.weaver.tools.Trace;

import java.util.UUID;

@Builder
public record Course (
        UUID id,
        String ref,
        String title,
        int credits,
        Track track
){}
