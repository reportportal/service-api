package com.epam.ta.reportportal.core.item.merge.strategy;

import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public abstract class AbstractSuiteMergeStrategy implements MergeStrategy {
    protected TestItemRepository testItemRepository;

    public AbstractSuiteMergeStrategy(TestItemRepository testItemRepository) {
        this.testItemRepository = testItemRepository;
    }

    @Override

    public abstract TestItem mergeTestItems(TestItem itemTarget, List<TestItem> items);

    public abstract boolean isTestItemAcceptableToMerge(TestItem item);

    private TestItem moveAllChildTestItems(TestItem itemTarget, TestItem itemSource) {
        for (TestItem childItem : testItemRepository.findAllDescendants(itemSource.getId())) {
            childItem.setParent(itemTarget.getId());
            childItem.setLaunchRef(itemTarget.getLaunchRef());

            if (childItem.getName().equals("UserManagement")){
                System.out.println();
            }
            List<String> path = new ArrayList<>(itemTarget.getPath());
            path.add(itemTarget.getId());
            childItem.setPath(path);

            setLaunchRefForChilds(childItem, itemTarget.getLaunchRef());
            testItemRepository.save(childItem);
            testItemRepository.delete(itemSource);
        }
        return itemTarget;
    }

    TestItem moveAllChildTestItems(TestItem itemTarget, List<TestItem> items) {
        TestItem result = items.stream().reduce(itemTarget, this::moveAllChildTestItems);
        mergeAllChildItems(result);
        return result;
    }

    private void setLaunchRefForChilds(TestItem testItemParent, String launchRef) {
        List<TestItem> childItems = testItemRepository.findAllDescendants(testItemParent.getId());
        if (testItemParent.getName().equals("UserManagement")){
            System.out.println();
        }
        for (TestItem child : childItems) {
            child.setLaunchRef(launchRef);
            List<String> path = new ArrayList<>(testItemParent.getPath());
            path.add(testItemParent.getId());
            child.setPath(path);
            testItemRepository.save(child);
            if (child.hasChilds()) {
                setLaunchRefForChilds(child, launchRef);
            }
        }
    }

    private void mergeAllChildItems(TestItem testItemParent) {
        List<TestItem> childItems = testItemRepository.findAllDescendants(testItemParent.getId());
        List<TestItem> suites = childItems.stream().filter(this::isTestItemAcceptableToMerge).collect(toList());
        List<List<TestItem>> combinedByName = new ArrayList<>();
        Set<String> names = suites.stream().map(TestItem::getName).collect(toSet());

        for (String name : names) {
            List<TestItem> suitesWithEqualName = suites.stream().filter(item -> item.getName().equals(name)).collect(toList());
            combinedByName.add(suitesWithEqualName);
        }
        for (List<TestItem> items : combinedByName) {
            moveAllChildTestItems(items.get(0), items.subList(1, items.size()));
        }
    }
}
