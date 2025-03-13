package icu.cykuta.beaconshield.providers;

public class DependencyNotEnabledException extends Exception {
    public DependencyNotEnabledException(Hook<?> hook) {
        super("Unable to find the dependency: " + hook.getName());
    }
}
