package org.noear.solon.serialization.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.CalendarSerializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import org.noear.solon.core.XContext;
import org.noear.solon.core.XRender;

import java.util.Calendar;
import java.util.Date;

import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;

public class JacksonRender implements XRender {
    ObjectMapper mapper = new ObjectMapper();
    ObjectMapper mapper_serialize = new ObjectMapper();

    private boolean _typedJson;
    public JacksonRender(boolean typedJson){
        _typedJson = typedJson;

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Calendar.class, new CalendarSerializer());
        simpleModule.addSerializer(Date.class, new DateSerializer());


        mapper.registerModule(simpleModule);

        mapper_serialize.enableDefaultTypingAsProperty(NON_FINAL, "@type");
        mapper_serialize.registerModule(simpleModule);
    }

    @Override
    public void render(Object obj, XContext ctx) throws Throwable {
        String txt = null;

        if (_typedJson) {
            //序列化处理
            //
            txt = mapper_serialize.writeValueAsString(obj);
        } else {
            //非序列化处理
            //
            if (obj == null) {
                return;
            }

            if (obj instanceof Throwable) {
                throw (Throwable) obj;
            }

            if (obj instanceof String) {
                ctx.output((String) obj); //不能做为json输出
                return;
            }

            txt = mapper.writeValueAsString(obj);
        }

        if(XPluginImp.output_meta) {
            ctx.headerSet("solon.serialization", "JacksonRender");
        }

        ctx.attrSet("output", txt);
        ctx.outputAsJson(txt);
    }
}
