package com.gymtracker.app.data.local

import com.gymtracker.app.data.local.entity.ExerciseEntity

object DefaultExercises {
    val list: List<ExerciseEntity> = listOf(
        // 1. الصدر (Chest) - 4
        ExerciseEntity(name = "بنش برس بالبار مستوي", category = "الصدر"),
        ExerciseEntity(name = "تجميع بالدمبل مائل", category = "الصدر"),
        ExerciseEntity(name = "تفتيح بالكيبل للصدر", category = "الصدر"),
        ExerciseEntity(name = "جهاز ضغط الصدر", category = "الصدر"),

        // 2. الظهر (Back) - 4
        ExerciseEntity(name = "سحب ظهر بالبار منحني", category = "الظهر"),
        ExerciseEntity(name = "سحب ظهر عمودي كيبل", category = "الظهر"),
        ExerciseEntity(name = "سحب دمبل فردي", category = "الظهر"),
        ExerciseEntity(name = "سحب أرضي بالكيبل جالس", category = "الظهر"),

        // 3. الأرجل (Legs) - 4
        ExerciseEntity(name = "سكوات بالبار", category = "الأرجل"),
        ExerciseEntity(name = "جهاز دفع الأرجل", category = "الأرجل"),
        ExerciseEntity(name = "ديدليفت روماني بالبار/الدمبل", category = "الأرجل"),
        ExerciseEntity(name = "جهاز مد أو ثني الأرجل", category = "الأرجل"),

        // 4. الأكتاف (Shoulders) - 3
        ExerciseEntity(name = "ضغط أكتاف بالبار واقف", category = "الأكتاف"),
        ExerciseEntity(name = "ضغط أكتاف بالدمبل جالس", category = "الأكتاف"),
        ExerciseEntity(name = "رفرفة جانبي بالدمبل", category = "الأكتاف"),

        // 5. البايسبس (Biceps) - 3
        ExerciseEntity(name = "مرجحة بايسبس بالبار", category = "البايسبس"),
        ExerciseEntity(name = "مرجحة بايسبس تبادل بالدمبل", category = "البايسبس"),
        ExerciseEntity(name = "مرجحة هامر بالكيبل بالحبل", category = "البايسبس"),

        // 6. الترايسبس (Triceps) - 3
        ExerciseEntity(name = "دفع ترايسبس بالكيبل بالحبل", category = "الترايسبس"),
        ExerciseEntity(name = "كسر الجمجمة بالبار EZ", category = "الترايسبس"),
        ExerciseEntity(name = "ضغط ترايسبس بالدمبل فوق الرأس", category = "الترايسبس"),

        // 7. السواعد (Forearms) - 3
        ExerciseEntity(name = "ثني المعصم بالبار جالس", category = "السواعد"),
        ExerciseEntity(name = "ثني المعصم العكسي بالبار أو الكيبل", category = "السواعد"),
        ExerciseEntity(name = "مرجحة معصم بالدمبل فردي", category = "السواعد"),

        // 8. الترابيس (Traps) - 3
        ExerciseEntity(name = "هزات أكتاف بالبار", category = "الترابيس"),
        ExerciseEntity(name = "هزات أكتاف بالدمبل", category = "الترابيس"),
        ExerciseEntity(name = "هزات أكتاف بالكيبل", category = "الترابيس"),

        // 9. البطن (Abs) - 3
        ExerciseEntity(name = "طحن البطن بالكيبل جالس", category = "البطن"),
        ExerciseEntity(name = "جهاز طحن البطن بالأوزان", category = "البطن"),
        ExerciseEntity(name = "دوران الجذع بالكيبل", category = "البطن")
    )
}
