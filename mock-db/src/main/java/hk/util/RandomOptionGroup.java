package hk.util;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Builder(builderClassName = "Builder")
public class RandomOptionGroup<T> {

    int totalWeight = 0;

    List<RanOpt<T>> optList = new ArrayList<>();

    public RandomOptionGroup() {
    }

    public RandomOptionGroup(int totalWeight, List<RanOpt<T>> optList) {
        this.totalWeight = totalWeight;
        this.optList = optList;
    }

    public static <T> Builder<T> builder() {
        return new RandomOptionGroup.Builder<T>();
    }

    public static class Builder<T> {
        List<RanOpt<T>> optList = new ArrayList<>();
        int totalWeight = 0;

        public Builder add(T value, int weight) {
            RanOpt<T> ranOpt = new RanOpt<>(value, weight);
            totalWeight += weight;
            for (int i = 0; i < weight; i++) {
                optList.add(ranOpt);
            }
            return this;
        }

        public RandomOptionGroup<T> build() {
            return new RandomOptionGroup<>(totalWeight, optList);
        }
    }

    public RandomOptionGroup(String... values) {
        for (String value : values) {
            totalWeight += 1;
            optList.add(new RanOpt<T>((T) value, 1));
        }
    }

    public RandomOptionGroup(RanOpt<T>... opts) {
        for (RanOpt<T> opt : opts) {
            totalWeight += opt.getWeight();
            for (int i = 0; i < opt.getWeight(); i++) {
                optList.add(opt);
            }
        }
    }

    public RandomOptionGroup(int trueWeight, int falseWeight) {
        RanOpt<Boolean> trueOpt = new RanOpt<>(true, trueWeight);
        RanOpt<Boolean> falseOpt = new RanOpt<>(false, falseWeight);
        this.totalWeight = trueWeight + falseWeight;
        for (int i = 0; i < trueWeight; i++) {
            optList.add((RanOpt<T>) trueOpt);
        }
        for (int i = 0; i < falseWeight; i++) {
            optList.add((RanOpt<T>) falseOpt);
        }
    }

    public T getValue() {
        int i = new Random().nextInt(totalWeight);
        return optList.get(i).getValue();
    }

    public RanOpt<T> getRandomOpt() {
        int i = new Random().nextInt(totalWeight);
        return optList.get(i);
    }

    public String getRandStringValue() {
        int i = new Random().nextInt(totalWeight);
        return (String) optList.get(i).getValue();
    }

    public Integer getRandIntValue() {
        int i = new Random().nextInt(totalWeight);
        return (Integer) optList.get(i).getValue();
    }

    public Boolean getRandBoolValue() {
        int i = new Random().nextInt(totalWeight);
        return (Boolean) optList.get(i).getValue();
    }

    public static void main(String[] args) {
        RanOpt<String> opt1 = new RanOpt<>("zhang3", 20);
        RanOpt<String> opt2 = new RanOpt<>("li4", 30);
        RanOpt<String> opt3 = new RanOpt<>("wang5", 50);
        RandomOptionGroup<String> randomOptionGroup = new RandomOptionGroup<>(opt1, opt2, opt3);
        for (int i = 0; i < 10; i++) {
            System.out.println(randomOptionGroup.getRandomOpt().getValue());
        }
    }
}