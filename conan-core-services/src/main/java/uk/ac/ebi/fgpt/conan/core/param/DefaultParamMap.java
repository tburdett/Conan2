package uk.ac.ebi.fgpt.conan.core.param;

import uk.ac.ebi.fgpt.conan.model.param.*;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 14/01/14
 * Time: 11:06
 * To change this template use File | Settings | File Templates.
 */
public class DefaultParamMap extends LinkedHashMap<ConanParameter, String> implements ParamMap {

    @Override
    public Set<ParamMapEntry> paramEntrySet() {

        Set<ParamMapEntry> all = new HashSet<>();

        for(Map.Entry<ConanParameter, String> entry : this.entrySet()) {
            all.add(new DefaultParamMapEntry(entry.getKey(), entry.getValue()));
        }

        return all;
    }

    @Override
    public List<ParamMapEntry> getOptionList() {

        List<ParamMapEntry> options = new ArrayList<>();
        for(Map.Entry<ConanParameter, String> entry : this.entrySet()) {
            if (entry.getKey().isOption()) {
                options.add(new DefaultParamMapEntry(entry.getKey(), entry.getValue()));
            }
        }

        return options;
    }

    @Override
    public List<ParamMapEntry> getArgList() {

        List<ParamMapEntry> args = new ArrayList<>(this.getNbArgs());

        for(Map.Entry<ConanParameter, String> entry : this.entrySet()) {
            if (entry.getKey().isArgument()) {
                args.add(new DefaultParamMapEntry(entry.getKey(), entry.getValue()));
            }
        }

        Collections.sort(args, new Comparator<ParamMapEntry>() {
            @Override
            public int compare(ParamMapEntry o1, ParamMapEntry o2) {
                return o1.getKey().getArgumentIndex() - o2.getKey().getArgumentIndex();
            }
        });


        return args;
    }

    @Override
    public List<ParamMapEntry> getRedirectionList() {

        List<ParamMapEntry> redirects = new ArrayList<>(this.getNbRedirects());

        for(Map.Entry<ConanParameter, String> entry : this.entrySet()) {
            if (entry.getKey().isRedirect()) {
                redirects.add(new DefaultParamMapEntry(entry.getKey(), entry.getValue()));
            }
        }

        return redirects;
    }

    public int getNbArgs() {

        int nbArgs = 0;
        for(ConanParameter param : this.keySet()) {
            if (param.isArgument()) {
                nbArgs++;
            }
        }

        return nbArgs;
    }

    public int getNbRedirects() {

        int nbRedirects = 0;
        for(ConanParameter param : this.keySet()) {
            if (param.isRedirect()) {
                nbRedirects++;
            }
        }

        return nbRedirects;
    }

    @Override
    public String buildOptionString(CommandLineFormat format) throws ConanParameterException {

        return this.buildOptionString(format, new ArrayList<ConanParameter>());
    }

    @Override
    public String buildOptionString(CommandLineFormat format, List<ConanParameter> exceptions) throws ConanParameterException {

        StringBuilder optionsString = new StringBuilder();

        List<ParamMapEntry> options = this.getOptionList();

        // Remove any options that we don't want to consider.
        removeExclusions(options, exceptions);

        if (!options.isEmpty()) {

            optionsString.append(format.buildOption(options.get(0)));

            for(int i = 1; i < options.size(); i++) {

                optionsString.append(" ").append(format.buildOption(options.get(i)));
            }
        }

        return optionsString.toString();
    }

    protected void removeExclusions(List<ParamMapEntry> entries, List<ConanParameter> exclusions) {

        for(ConanParameter param : exclusions) {
            for(ParamMapEntry entry : entries) {
                if (entry.getKey().equals(param)) {
                    entries.remove(entry);
                    break;
                }
            }
        }
    }

    @Override
    public String buildArgString() {

        StringBuilder argsString = new StringBuilder();

        List<ParamMapEntry> args = this.getArgList();

        if (!args.isEmpty()) {

            argsString.append(args.get(0).getValue());

            for(int i = 1; i < args.size(); i++) {

                argsString.append(" ").append(args.get(i).getValue());
            }
        }

        return argsString.toString();
    }

    @Override
    public String buildRedirectionString() {

        StringBuilder redirectString = new StringBuilder();

        List<ParamMapEntry> args = this.getRedirectionList();

        if (args.size() > 1) {
            throw new IllegalArgumentException("Can only redirect output to a single file");
        }

        if (!args.isEmpty()) {

            redirectString.append(args.get(0).getValue());
        }

        return redirectString.toString();
    }

    @Override
    public void validate(ProcessParams allParams) throws ConanParameterException {

        for(ConanParameter param : allParams.getConanParameters()) {

            if (!param.isOptional() && !this.containsKey(param)) {

                throw new ConanParameterException("Parameter: " + param.getIdentifier() +
                        "; is mandatory but is not found in this Parameter Map");
            }


            // For the params we do have check they are valid
            if (this.containsKey(param)) {

                String value = this.get(param);
                if (!param.validateParameterValue(value)) {
                    throw new ConanParameterException("Parameter: " + param.getIdentifier() + "; with value: " + value +
                         "; is not valid");
                }
            }
        }

    }

}
