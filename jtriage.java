///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS https://github.com/maxandersen/fluent-process/tree/shell
//JAVA 16+

import com.ongres.process.FluentProcess;

import static java.lang.System.*;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static com.ongres.process.FluentProcess.$;

public class jtriage {
    public static void main(String... args) {
        var JSON_FIELDS = "createdAt,labels,number,title";
        var TEMPLATE = """
        {{- printf "Showing issues to triage\\n\\n" -}}
          {{- tablerow "ID" "TITLE" "LABELS" "CREATED AT" -}}
          {{- range $issue := . -}}
            {{- $found := false -}}
            {{- range $index, $label := $issue.labels -}}
              {{- if not $found -}}
                {{- if eq $label.name "p1" "p2" "p3" "core" "help wanted" "tracking issue" "needs-design" "blocked" "needs-user-input" -}}
                  {{- $found = true -}}
                {{- end -}}
              {{- end -}}
            {{- end -}}
            {{- if not $found -}}
              {{- $number := printf "#%v" $issue.number | autocolor "green" -}}
              {{- $labels := $issue.labels | pluck "name" | join ", " -}}
              {{- $timeAgo := timeago $issue.createdAt | autocolor "black+h" -}}
              {{- tablerow $number $issue.title $labels $timeAgo -}}
            {{- end -}}
          {{- end -}}
          """;

        String quotedargs = Arrays.stream(args).map(s -> "\"" + s + "\"")
        .collect(Collectors.joining(" "));
        FluentProcess.builder("gh issue list "+quotedargs+" --json \"${JSON_FIELDS}\" --template \"${TEMPLATE}\"")
                .as$()
                .environment("TEMPLATE",TEMPLATE)
                .environment("JSON_FIELDS", JSON_FIELDS)
                .environment("CLICOLOR_FORCE", "true")
                .allowedExitCode(1)
                .start()
                .stream().forEach(System.out::println);
    }
}
