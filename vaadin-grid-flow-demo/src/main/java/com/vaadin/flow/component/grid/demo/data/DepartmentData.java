package com.vaadin.flow.component.grid.demo.data;

import com.vaadin.flow.component.grid.demo.entity.Department;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DepartmentData {

	private List<Department> list = new ArrayList<>();

	public DepartmentData() {

		list.add(new Department(1, "Product", null,"Manager is Paivi"));
		list.add(new Department(11, "Flow", list.get(0),"Direct manager is Pekka"));
		list.add(new Department(111, "Flow Core", list.get(1),"Direct manager is Pekka"));
		list.add(new Department(111, "Flow Components", list.get(1),"Direct manager is Gilberto"));
		list.add(new Department(12, "Design", list.get(0),"Direct manager is Pekka"));
		list.add(new Department(13, "DJO", list.get(0),"Direct manager is Thomas"));
		list.add(new Department(14, "Component", list.get(0),"Direct manager is Tomi"));
		list.add(new Department(2, "HR", null,"Direct manager is Anne"));
		list.add(new Department(21, "Office", list.get(7),"Direct manager is Anu"));
		list.add(new Department(22, "Employee",list.get(7),"Direct manager is Minnah"));
		list.add(new Department(3,"Marketing",null,"Manager Niko van Eeghen"));
		list.add(new Department(31,"Growth",list.get(10),"Direct manager is Ömer Tümer"));
		list.add(new Department(32,"Demand Generation",list.get(10),"Direct manager is Marcus"));
		list.add(new Department(33,"Product Marketing",list.get(10),"Direct manager is Pekka"));
		list.add(new Department(34,"Brand Experience",list.get(10),"Direct manager is Eero"));

	}

	public List<Department> getDepartments() {
		return list;
	}

	public List<Department> getRootDepartments() {
		return list.stream().filter(department -> department.getParent() == null)
				.collect(Collectors.toList());
	}

	public List<Department> getChildDepartments(Department parent) {
		return list.stream().filter(department -> Objects.equals(department.getParent(), parent))
				.collect(Collectors.toList());
	}
}
